describe("Login Feature", () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it("should open login page successfully", () => {
    cy.visit("http://localhost:8080/login");

    cy.contains("Sign In");
    cy.get("#username").should("exist");
    cy.get("#password").should("exist");
    cy.get("#loginForm").should("exist");
  });

  it("should allow typing username and password", () => {
    cy.visit("http://localhost:8080/login");

    cy.get("#username").type("admin");
    cy.get("#password").type("123456");

    cy.get("#username").should("have.value", "admin");
    cy.get("#password").should("have.value", "123456");
  });

  it("should show error message when login fails", () => {
    cy.intercept("POST", "/api/auth/login", {
      statusCode: 401,
      body: {
        message: "Sai tài khoản hoặc mật khẩu",
      },
    }).as("loginFail");

    cy.visit("http://localhost:8080/login");

    cy.get("#username").type("admin");
    cy.get("#password").type("wrongpass");
    cy.get('button[type="submit"]').click();

    cy.wait("@loginFail");

    cy.get("#errorMessage")
      .should("be.visible")
      .and("contain", "Sai tài khoản hoặc mật khẩu");
  });

  it("should redirect to dashboard when login succeeds", () => {
    cy.intercept("POST", "/api/auth/login", {
      statusCode: 200,
      body: {
        fullName: "Admin User",
        username: "admin",
        role: "ADMIN",
      },
    }).as("loginSuccess");

    cy.visit("http://localhost:8080/login");

    cy.get("#username").type("admin");
    cy.get("#password").type("123456");
    cy.get('button[type="submit"]').click();

    cy.wait("@loginSuccess");
    cy.url().should("include", "/dashboard");
  });
});
