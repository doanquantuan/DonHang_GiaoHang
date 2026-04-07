describe("Order List Page", () => {
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

    cy.intercept("GET", "/api/orders", [
      {
        id: 1,
        customerName: "Nguyễn Văn A",
        phone: "0901234567",
        address: "123 Lê Lợi, TP.HCM",
        orderDate: "2026-03-29T10:00:00",
        totalAmount: 150000,
        status: "NEW",
      },
      {
        id: 2,
        customerName: "Trần Thị B",
        phone: "0912345678",
        address: "456 Nguyễn Huệ, TP.HCM",
        orderDate: "2026-03-28T15:30:00",
        totalAmount: 250000,
        status: "COMPLETED",
      },
    ]).as("getOrders");

    cy.visit("http://localhost:8080/orders");
    cy.wait("@getOrders");
  });

  it("should display filter controls", () => {
    cy.get("#q").should("exist");
    cy.get("#filterStatus").should("exist");
    cy.get("#fromDate").should("exist");
    cy.get("#toDate").should("exist");
  });

  it("should display orders in table", () => {
    cy.contains("DH-1").should("be.visible");
    cy.contains("Nguyễn Văn A").should("be.visible");
    cy.contains("DH-2").should("be.visible");
    cy.contains("Trần Thị B").should("be.visible");
  });

  it("should filter orders by customer name", () => {
    cy.get("#q").type("Nguyễn Văn A");

    cy.contains("Nguyễn Văn A").should("be.visible");
    cy.contains("Trần Thị B").should("not.exist");
  });

  it("should filter orders by status", () => {
    cy.get("#filterStatus").select("NEW");

    cy.contains("DH-1").should("be.visible");
    cy.contains("DH-2").should("not.exist");
  });

  it("should clear filters", () => {
    cy.get("#q").type("Nguyễn Văn A");
    cy.get("#filterStatus").select("NEW");

    cy.contains("Xoá lọc").click();

    cy.get("#q").should("have.value", "");
    cy.get("#filterStatus").should("have.value", "");
    cy.contains("Nguyễn Văn A").should("be.visible");
    cy.contains("Trần Thị B").should("be.visible");
  });

  it("should open delete modal", () => {
    cy.get(".ol-btn-del").first().click();

    cy.get("#deleteOverlay").should("have.class", "show");
    cy.contains("Xác nhận xoá đơn hàng").should("be.visible");
  });

  it("should close delete modal", () => {
    cy.get(".ol-btn-del").first().click();
    cy.contains("Huỷ bỏ").click();

    cy.get("#deleteOverlay").should("not.have.class", "show");
  });

  it("should check all checkboxes", () => {
    cy.get("#checkAll").check();

    cy.get(".row-cb").each(($cb) => {
      cy.wrap($cb).should("be.checked");
    });
  });
});
