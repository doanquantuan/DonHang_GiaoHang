describe("Create Delivery", () => {
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
    // Mock danh sách đơn hàng
    cy.intercept("GET", "/api/orders", [
      {
        id: 1,
        customerName: "Nguyễn Văn A",
        phone: "0909123456",
        address: "123 Lê Lợi, Quận 1",
        totalAmount: 120000,
        paymentMethod: "COD",
        status: "NEW",
        shipperName: "",
        deliveryNote: "Giao trước 10h",
        expectedTime: "2026-03-31 08:00 – 12:00",
      },
      {
        id: 2,
        customerName: "Trần Thị B",
        phone: "0911222333",
        address: "456 Nguyễn Huệ, Quận 1",
        totalAmount: 180000,
        paymentMethod: "Chuyển khoản",
        status: "NEW",
        shipperName: "",
      },
    ]).as("getOrders");

    // Mock danh sách shipper
    cy.intercept("GET", "/api/deliveries/shippers", [
      {
        fullName: "Nguyễn Tài Xế",
        username: "shipper01",
        phone: "0988111222",
        vehicleInfo: "Wave RSX",
      },
      {
        fullName: "Lê Văn Giao",
        username: "shipper02",
        phone: "0977333444",
        vehicleInfo: "Exciter 150",
      },
    ]).as("getShippers");

    // Mock submit create delivery (thực ra là update order)
    cy.intercept("PUT", "/api/orders/1", {
      statusCode: 200,
      body: {
        message: "Tạo chuyến giao thành công",
      },
    }).as("submitDelivery");

    cy.visit("http://localhost:8080/deliveries/create");
    cy.wait("@getOrders");
    cy.wait("@getShippers");
  });

  it("Hiển thị đúng giao diện trang tạo chuyến giao", () => {
    cy.contains("GIAO HÀNG").should("be.visible");
    cy.contains("Tạo chuyến giao mới").should("be.visible");
    cy.contains("Chọn đơn hàng").should("be.visible");
    cy.contains("Thông tin giao hàng").should("be.visible");
    cy.contains("Trạng thái ban đầu").should("be.visible");
    cy.contains("button", "Tạo chuyến giao").should("be.visible");
  });

  it("Hiển thị danh sách đơn hàng chưa phân công", () => {
    cy.contains("DH-1").should("be.visible");
    cy.contains("Nguyễn Văn A").should("be.visible");

    cy.contains("DH-2").should("be.visible");
    cy.contains("Trần Thị B").should("be.visible");
  });

  it("Tìm kiếm đơn hàng hoạt động đúng", () => {
    cy.get("#orderSearch").type("Nguyễn Văn A");

    cy.contains("DH-1").should("be.visible");
    cy.contains("Nguyễn Văn A").should("be.visible");

    cy.contains("DH-2").should("not.exist");
  });

  it("Chọn đơn hàng sẽ hiển thị preview", () => {
    cy.contains("DH-1").click();

    cy.get("#selectedPreview").should("be.visible");
    cy.contains("Đơn hàng đã chọn").should("be.visible");
    cy.contains("Nguyễn Văn A").should("be.visible");
    cy.contains("0909123456").should("be.visible");
    cy.contains("123 Lê Lợi, Quận 1").should("be.visible");
  });

  it("Chọn shipper sẽ hiển thị thông tin tài xế", () => {
    cy.get("#shipperSelect").select("Nguyễn Tài Xế");

    cy.get("#shipperDetail").should("be.visible");
    cy.contains("Nguyễn Tài Xế").should("be.visible");
    cy.contains("0988111222").should("be.visible");
    cy.contains("Wave RSX").should("be.visible");
  });

  it("Không cho submit nếu chưa chọn đơn hàng", () => {
    cy.get("#shipperSelect").select("Nguyễn Tài Xế");
    cy.contains("button", "Tạo chuyến giao").click();

    // Vì toast của bạn render bằng layout riêng,
    // nên chỉ kiểm tra request không được gửi
    cy.get("@submitDelivery.all").should("have.length", 0);
  });

  it("Không cho submit nếu chưa chọn tài xế", () => {
    cy.contains("DH-1").click();
    cy.contains("button", "Tạo chuyến giao").click();

    cy.get("#shipperSelect").should("have.class", "invalid");
    cy.get("@submitDelivery.all").should("have.length", 0);
  });

  it("Submit thành công khi chọn đơn hàng + shipper", () => {
    cy.contains("DH-1").click();

    cy.get("#shipperSelect").select("Nguyễn Tài Xế");
    cy.get("#deliveryDate").type("2026-04-01");
    cy.get("#timeFrom").clear().type("09:00");
    cy.get("#timeTo").clear().type("11:00");
    cy.get("#deliveryNote").clear().type("Giao cẩn thận");
    cy.get("#initStatus").select("SHIPPING");

    cy.contains("button", "Tạo chuyến giao").click();

    cy.wait("@submitDelivery").then((interception) => {
      expect(interception.request.body.shipperName).to.equal("Nguyễn Tài Xế");
      expect(interception.request.body.deliveryNote).to.equal("Giao cẩn thận");
      expect(interception.request.body.status).to.equal("SHIPPING");
      expect(interception.request.body.expectedTime).to.equal(
        "2026-04-01 09:00 – 11:00",
      );
    });
  });

  it("Tự động điền ghi chú và thời gian từ order đã chọn", () => {
    cy.contains("DH-1").click();

    cy.get("#deliveryNote").should("have.value", "Giao trước 10h");
    cy.get("#deliveryDate").should("have.value", "2026-03-31");
    cy.get("#timeFrom").should("have.value", "08:00");
    cy.get("#timeTo").should("have.value", "12:00");
  });
});
