describe("Update Order", () => {
  const orderId = 5;

  const mockOrder = {
    id: orderId,
    customerName: "Nguyễn Văn A",
    phone: "0909123456",
    email: "vana@example.com",
    address: "123 Nguyễn Trãi, Q1",
    status: "NEW",
    paymentMethod: "COD",
    paymentStatus: "Chưa thu",
    shippingFee: 15000,
    discount: 0,
    deliveryNote: "Gọi trước khi giao",
    expectedTime: "2026-03-31 08:00 – 12:00",
    shipperName: "Trần Văn Shipper",
    delivery: {
      id: 12,
      shipperName: "Trần Văn Shipper",
      note: "Gọi trước khi giao",
      expectedTime: "2026-03-31 08:00 – 12:00",
    },
    orderDetails: [
      {
        productName: "Trà sữa truyền thống",
        quantity: 2,
        price: 30000,
      },
      {
        productName: "Hồng trà sữa",
        quantity: 1,
        price: 35000,
      },
    ],
  };

  const mockShippers = [
    {
      username: "shipper1",
      fullName: "Trần Văn Shipper",
      phone: "0909888777",
      vehicleInfo: "Wave RSX",
    },
    {
      username: "shipper2",
      fullName: "Lê Minh Giao",
      phone: "0909111222",
      vehicleInfo: "Vision",
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

    cy.intercept("GET", `/api/orders/${orderId}`, {
      statusCode: 200,
      body: mockOrder,
    }).as("getOrder");

    cy.intercept("GET", "/api/deliveries/shippers", {
      statusCode: 200,
      body: mockShippers,
    }).as("getShippers");

    cy.visit(`http://localhost:8080/orders/${orderId}/edit`);

    cy.wait("@getOrder");
    cy.wait("@getShippers");
  });

  it("Hiển thị form update với dữ liệu có sẵn", () => {
    cy.contains("ĐƠN HÀNG").should("be.visible");
    cy.contains(`Chỉnh sửa đơn hàng #DH-${orderId}`).should("be.visible");

    cy.get("#customerName").should("have.value", "Nguyễn Văn A");
    cy.get("#phone").should("have.value", "0909123456");
    cy.get("#email").should("have.value", "vana@example.com");
    cy.get("#address").should("have.value", "123 Nguyễn Trãi, Q1");
    cy.get("#deliveryNote").should("have.value", "Gọi trước khi giao");

    cy.get("#status").should("have.value", "NEW");
    cy.get("#paymentMethod").should("have.value", "COD");

    cy.get("#paid").should("not.be.checked");
    cy.get("#paid").should("be.disabled");

    cy.get("#shipperName").should("have.value", "Trần Văn Shipper");

    cy.get("#shippingFee").should("have.value", "15000");

    cy.get("#ptbody tr").should("have.length", 2);
    cy.get("#subtotal").should("contain", "95.000đ");
    cy.get("#grandtotal").should("contain", "110.000đ");
  });

  it("Cho phép cập nhật đơn hàng thành công", () => {
    cy.intercept("PUT", `/api/orders/${orderId}`, (req) => {
      expect(req.body.customerName).to.equal("Nguyễn Văn B");
      expect(req.body.phone).to.equal("0911222333");
      expect(req.body.address).to.equal("456 Lê Lợi, Q1");
      expect(req.body.status).to.equal("CONFIRMED");
      expect(req.body.paymentMethod).to.equal("Chuyển khoản");
      expect(req.body.paymentStatus).to.equal("Đã thanh toán");
      expect(req.body.shipperName).to.equal("Lê Minh Giao");
      expect(req.body.shippingFee).to.equal(20000);

      expect(req.body.orderDetails).to.have.length(2);
      expect(req.body.orderDetails[0].productName).to.equal("Trà sữa matcha");

      req.reply({
        statusCode: 200,
        body: {
          message: "Cập nhật thành công",
        },
      });
    }).as("updateOrder");

    cy.intercept("PUT", "/api/deliveries/12/status", {
      statusCode: 200,
      body: {
        message: "Sync delivery thành công",
      },
    }).as("syncDelivery");

    // Sửa thông tin khách hàng
    cy.get("#customerName").clear().type("Nguyễn Văn B");
    cy.get("#phone").clear().type("0911222333");
    cy.get("#address").clear().type("456 Lê Lợi, Q1");

    // Sửa thanh toán
    cy.get("#paymentMethod").select("Chuyển khoản");
    cy.get("#paid").should("not.be.disabled").check({ force: true });

    // Đổi trạng thái
    cy.get("#status").select("Đã xác nhận");

    // Đổi shipper
    cy.get("#shipperName").select("Lê Minh Giao");

    // Đổi phí ship
    cy.get("#shippingFee").clear().type("20000");

    // Sửa sản phẩm đầu tiên
    cy.get("#ptbody tr")
      .eq(0)
      .within(() => {
        cy.get('input[type="text"]').clear().type("Trà sữa matcha");
        cy.get('input[type="number"]').eq(0).clear().type("3"); // quantity
        cy.get('input[type="number"]').eq(1).clear().type("40000"); // price
      });

    cy.get("#submitBtn").click();

    cy.wait("@updateOrder");
    cy.wait("@syncDelivery");

    // Redirect về danh sách
    cy.url().should("include", "/orders");
  });

  it("Hiển thị lỗi validate nếu để trống field bắt buộc", () => {
    cy.get("#customerName").clear();
    cy.get("#phone").clear();
    cy.get("#address").clear();

    cy.get("#submitBtn").click();

    cy.get("#customerName").should("have.class", "invalid");
    cy.get("#phone").should("have.class", "invalid");
    cy.get("#address").should("have.class", "invalid");
  });

  it("Nếu payment method là COD thì không được tick đã thanh toán", () => {
    cy.get("#paymentMethod").select("Tiền mặt (COD)");
    cy.get("#paid").should("be.disabled");
    cy.get("#paid").should("not.be.checked");
  });

  it("Nếu status là COMPLETED thì auto tick đã thanh toán", () => {
    cy.get("#paymentMethod").select("Chuyển khoản");
    cy.get("#paid").should("not.be.disabled");

    cy.get("#status").select("Hoàn thành");

    cy.get("#paid").should("be.checked");
    cy.get("#paid").should("be.disabled");
  });
});
