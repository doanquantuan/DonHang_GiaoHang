describe("Delivery List", () => {
  const deliveries = [
    {
      id: 1,
      shipperName: "Nguyễn Tài Xế",
      expectedTime: "2026-03-31 08:00 – 12:00",
      status: "WAITING",
      order: {
        id: 101,
        customerName: "Nguyễn Văn A",
        address: "123 Lê Lợi, Quận 1",
      },
    },
    {
      id: 2,
      shipperName: "Trần Giao Hàng",
      expectedTime: "2026-03-31 13:00 – 17:00",
      status: "DELIVERING",
      order: {
        id: 102,
        customerName: "Trần Thị B",
        address: "456 Nguyễn Huệ, Quận 1",
      },
    },
    {
      id: 3,
      shipperName: "",
      expectedTime: "2026-03-31 18:00 – 20:00",
      status: "FAILED",
      order: {
        id: 103,
        customerName: "Lê Văn C",
        address: "789 Hai Bà Trưng, Quận 3",
      },
    },
    {
      id: 4,
      shipperName: "Phạm Tài Xế",
      expectedTime: "2026-03-31 09:00 – 11:00",
      status: "DONE",
      order: {
        id: 104,
        customerName: "Phạm Thị D",
        address: "12 Điện Biên Phủ, Bình Thạnh",
      },
    },
  ];

  const shippers = [
    {
      fullName: "Nguyễn Tài Xế",
      username: "shipper1",
    },
    {
      fullName: "Trần Giao Hàng",
      username: "shipper2",
    },
  ];

  beforeEach(() => {
    cy.visit("http://localhost:8080/login", {
      onBeforeLoad(win) {
        win.localStorage.setItem(
          "loggedInUser",
          JSON.stringify({
            username: "admin",
            fullName: "Admin User",
            role: "ADMIN",
          }),
        );
      },
    });

    cy.intercept("GET", "/api/deliveries", {
      statusCode: 200,
      body: deliveries,
    }).as("getDeliveries");

    cy.intercept("GET", "/api/deliveries/shippers", {
      statusCode: 200,
      body: shippers,
    }).as("getShippers");

    cy.intercept("PUT", /\/api\/deliveries\/\d+\/status/, (req) => {
      req.reply({
        statusCode: 200,
        body: { message: "Updated successfully" },
      });
    }).as("updateDeliveryStatus");

    cy.intercept("DELETE", /\/api\/deliveries\/\d+/, {
      statusCode: 200,
      body: { message: "Deleted successfully" },
    }).as("deleteDelivery");

    cy.visit("http://localhost:8080/deliveries");
    cy.wait("@getDeliveries");
    cy.wait("@getShippers");
  });

  it("Hiển thị đúng tiêu đề và nút tạo chuyến giao", () => {
    cy.get("h1.page-title")
      .should("be.visible")
      .and("contain", "Quản lý giao hàng");

    cy.contains("button", "Tạo chuyến giao").should("be.visible");
  });

  it("Hiển thị thống kê đúng", () => {
    cy.get("#statWaiting").should("contain", "1");
    cy.get("#statDelivering").should("contain", "1");
    cy.get("#statDone").should("contain", "1");
    cy.get("#statFailed").should("contain", "1");

    cy.get("#cntAll").should("contain", "4");
    cy.get("#cntWaiting").should("contain", "1");
    cy.get("#cntDelivering").should("contain", "1");
    cy.get("#cntDone").should("contain", "1");
    cy.get("#cntFailed").should("contain", "1");
  });

  it("Hiển thị danh sách giao hàng trong bảng", () => {
    cy.get("#delBody").should("contain", "GH-1");
    cy.get("#delBody").should("contain", "DH-101");
    cy.get("#delBody").should("contain", "Nguyễn Văn A");

    cy.get("#delBody").should("contain", "GH-2");
    cy.get("#delBody").should("contain", "DH-102");
    cy.get("#delBody").should("contain", "Trần Thị B");

    cy.get("#delBody").should("contain", "GH-3");
    cy.get("#delBody").should("contain", "Chưa phân công");
  });

  it("Chuyển tab WAITING chỉ hiện đơn WAITING", () => {
    cy.get("#tab-WAITING").click();

    cy.get("#delBody").should("contain", "GH-1");
    cy.get("#delBody").should("not.contain", "GH-2");
    cy.get("#delBody").should("not.contain", "GH-3");
    cy.get("#delBody").should("not.contain", "GH-4");
  });

  it("Chuyển tab DELIVERING chỉ hiện đơn DELIVERING", () => {
    cy.get("#tab-DELIVERING").click();

    cy.get("#delBody").should("contain", "GH-2");
    cy.get("#delBody").should("not.contain", "GH-1");
    cy.get("#delBody").should("not.contain", "GH-3");
    cy.get("#delBody").should("not.contain", "GH-4");
  });

  it("Click stat card sẽ chuyển tab tương ứng", () => {
    cy.contains(".stat-card", "Chờ lấy hàng").click();

    cy.get("#tab-WAITING").should("have.class", "active");
    cy.get("#delBody").should("contain", "GH-1");
    cy.get("#delBody").should("not.contain", "GH-2");
  });

  it("Mở modal phân công tài xế khi delivery chưa có shipper", () => {
    cy.contains("#drow-3 .btn-sm-assign", "Phân công")
      .should("be.visible")
      .click();

    cy.get("#assignModal").should("have.class", "show");
    cy.get("#assignDesc").should("contain", "DH-103");
    cy.get("#shipperSel").should("be.visible");
  });

  it("Không cho xác nhận phân công nếu chưa chọn tài xế", () => {
    cy.contains("#drow-3 .btn-sm-assign", "Phân công").click();

    cy.contains("button", "Xác nhận phân công").click();

    cy.get("#shipperErr").should("be.visible");
    cy.get("#shipperSel").should("have.class", "invalid");
  });

  it("Phân công tài xế thành công", () => {
    cy.contains("#drow-3 .btn-sm-assign", "Phân công").click();

    cy.get("#shipperSel").select("Nguyễn Tài Xế");
    cy.contains("button", "Xác nhận phân công").click();

    cy.wait("@updateDeliveryStatus")
      .its("request.body")
      .should((body) => {
        expect(body.status).to.equal("WAITING");
        expect(body.shipperName).to.equal("Nguyễn Tài Xế");
      });
  });

  it("Mở modal xác nhận khi đổi trạng thái", () => {
    cy.get("#sel-1").select("DELIVERING");

    cy.get("#statusModal").should("have.class", "show");
    cy.get("#statusDesc").should("contain", "GH-1");
    cy.get("#statusDesc").should("contain", "Chờ lấy");
    cy.get("#statusDesc").should("contain", "Đang giao");
  });

  it("Xác nhận đổi trạng thái thành công", () => {
    cy.get("#sel-1").select("DELIVERING");
    cy.contains("#statusModal button", "Xác nhận").click();

    cy.wait("@updateDeliveryStatus")
      .its("request.body")
      .should((body) => {
        expect(body.status).to.equal("DELIVERING");
      });
  });

  it("Nếu đổi trạng thái sang DONE thì phải gửi deliveryDate", () => {
    cy.get("#sel-1").select("DONE");
    cy.contains("#statusModal button", "Xác nhận").click();

    cy.wait("@updateDeliveryStatus")
      .its("request.body")
      .should((body) => {
        expect(body.status).to.equal("DONE");
        expect(body.deliveryDate).to.exist;
      });
  });

  it("Huỷ modal đổi trạng thái thì rollback dropdown", () => {
    cy.get("#sel-1").select("DELIVERING");

    cy.get("#statusModal").should("have.class", "show");
    cy.contains("#statusModal button", "Huỷ bỏ").click();

    cy.get("#sel-1").should("have.value", "WAITING");
  });

  it("Mở modal xoá delivery", () => {
    cy.contains("#drow-1 .btn-sm-del", "🗑").click();

    cy.get("#deleteModal").should("have.class", "show");
    cy.get("#deleteDesc").should("contain", "GH-1");
    cy.get("#deleteDesc").should("contain", "DH-101");
  });

  it("Xoá delivery thành công", () => {
    cy.contains("#drow-1 .btn-sm-del", "🗑").click();
    cy.contains("#deleteModal button", "Xoá chuyến giao").click();

    cy.wait("@deleteDelivery");
  });
});
