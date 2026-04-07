describe("Create Order Page", () => {
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

    cy.visit("http://localhost:8080/orders/create");
  });

  it("should open create order page successfully", () => {
    cy.contains("Thêm đơn hàng mới");
    cy.get("#customerName").should("exist");
    cy.get("#phone").should("exist");
    cy.get("#address").should("exist");
    cy.get("#paymentMethod").should("exist");
    cy.get("#submitBtn").should("exist");
  });

  it("should have 1 product row by default", () => {
    cy.get("#ptbody tr").should("have.length", 1);
  });

  it("should add a new product row when clicking 'Thêm sản phẩm'", () => {
    cy.get("#ptbody tr").should("have.length", 1);
    cy.get(".btn-add-row").click();
    cy.get("#ptbody tr").should("have.length", 2);
  });

  it("should calculate subtotal and grand total correctly", () => {
    cy.get("#ptbody tr")
      .first()
      .within(() => {
        cy.get('input[type="text"]').type("Áo thun");
        cy.get('input[type="number"]').eq(0).clear().type("2");
        cy.get('input[type="number"]').eq(1).clear().type("50000");
      });

    cy.get("#shippingFee").clear().type("30000");

    cy.get("#subtotal").should("contain", "100.000đ");
    cy.get("#grandtotal").should("contain", "130.000đ");
  });

  it("should validate required fields when submit empty form", () => {
    cy.get("#submitBtn").click();

    cy.get("#customerName").should("have.class", "invalid");
    cy.get("#phone").should("have.class", "invalid");
    cy.get("#address").should("have.class", "invalid");
  });

  it("should allow filling customer information", () => {
    cy.get("#customerName").type("Nguyễn Văn A");
    cy.get("#phone").type("0909123456");
    cy.get("#email").type("vana@example.com");
    cy.get("#address").type("123 Lê Lợi, TP.HCM");
    cy.get("#deliveryNote").type("Giao giờ hành chính");

    cy.get("#customerName").should("have.value", "Nguyễn Văn A");
    cy.get("#phone").should("have.value", "0909123456");
    cy.get("#email").should("have.value", "vana@example.com");
    cy.get("#address").should("have.value", "123 Lê Lợi, TP.HCM");
    cy.get("#deliveryNote").should("have.value", "Giao giờ hành chính");
  });

  it("should send correct payload when submitting order", () => {
    cy.intercept("POST", "/api/orders", (req) => {
      expect(req.body.customerName).to.equal("Nguyễn Văn A");
      expect(req.body.phone).to.equal("0909123456");
      expect(req.body.address).to.equal("123 Lê Lợi, TP.HCM");
      expect(req.body.paymentMethod).to.equal("COD");
      expect(req.body.paymentStatus).to.equal("Chưa thu");
      expect(req.body.shippingFee).to.equal(30000);
      expect(req.body.orderDetails).to.have.length(1);
      expect(req.body.orderDetails[0].productName).to.equal("Áo thun");
      expect(req.body.orderDetails[0].quantity).to.equal(2);
      expect(req.body.orderDetails[0].price).to.equal(50000);

      req.reply({
        statusCode: 201,
        body: { id: 999 },
      });
    }).as("createOrder");

    cy.get("#customerName").type("Nguyễn Văn A");
    cy.get("#phone").type("0909123456");
    cy.get("#address").type("123 Lê Lợi, TP.HCM");

    cy.get("#paymentMethod").select("COD");

    cy.get("#ptbody tr")
      .first()
      .within(() => {
        cy.get('input[type="text"]').type("Áo thun");
        cy.get('input[type="number"]').eq(0).clear().type("2");
        cy.get('input[type="number"]').eq(1).clear().type("50000");
      });

    cy.get("#shippingFee").clear().type("30000");

    cy.get("#submitBtn").click();

    cy.wait("@createOrder");
  });
});

describe("COD Payment Bug", () => {
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

    cy.visit("http://localhost:8080/orders/create");
  });

  it("should NOT allow checking 'Đã thanh toán' when payment method is COD", () => {
    cy.get("#paymentMethod").select("COD");
    cy.get("#paid").should("be.disabled").and("not.be.checked");
  });

  it("should allow checking paid when payment method is bank transfer", () => {
    cy.get("#paymentMethod").select("Chuyển khoản");
    cy.get("#paid").should("not.be.disabled");
    cy.get("#paid").check().should("be.checked");
  });
});
