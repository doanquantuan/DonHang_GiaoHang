USE OrderDeliveryDB;
GO

-- =======================================================
-- TẠO BẢNG USERS (ĐĂNG NHẬP / ĐĂNG KÝ)
-- =======================================================
CREATE TABLE Users (
    id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER', -- Các role dự kiến: ADMIN, SHIPPER, USER
    created_at DATETIME DEFAULT GETDATE(),
    is_active BIT DEFAULT 1
);

-- =======================================================
-- TẠO BẢNG ORDERS (ĐƠN HÀNG)
-- =======================================================
CREATE TABLE Orders (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    customer_name NVARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),               -- Bổ sung từ Wireframe
    address NVARCHAR(255),
    order_date DATETIME DEFAULT GETDATE(),
    shipping_fee DECIMAL(18,2) DEFAULT 0, -- Phí giao hàng từ Wireframe
    discount DECIMAL(18,2) DEFAULT 0,     -- Giảm giá từ Wireframe
    total_amount DECIMAL(18,2),
    status VARCHAR(20) DEFAULT 'NEW',
    payment_method NVARCHAR(50),      -- VD: Tiền mặt (COD), Chuyển khoản
    payment_status NVARCHAR(50),      -- VD: Chưa thu, Đã thanh toán
    updated_at DATETIME
);

-- =======================================================
-- TẠO BẢNG ORDER_DETAILS (CHI TIẾT ĐƠN HÀNG - SẢN PHẨM)
-- =======================================================
CREATE TABLE Order_Details (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    order_id BIGINT NOT NULL,
    product_name NVARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    CONSTRAINT FK_OrderDetails_Orders FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE
);

-- =======================================================
-- TẠO BẢNG DELIVERIES (GIAO HÀNG)
-- =======================================================
CREATE TABLE Deliveries (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    order_id BIGINT UNIQUE NOT NULL,  -- UNIQUE vì quan hệ 1-1 với Orders
    shipper_name NVARCHAR(255),
    shipper_phone VARCHAR(20),        -- Bổ sung SĐT tài xế từ Wireframe
    vehicle_info NVARCHAR(100),       -- Bổ sung Biển số xe từ Wireframe
    status VARCHAR(20) DEFAULT 'WAITING',
    expected_time NVARCHAR(100),      -- Thời gian giao dự kiến (VD: 08:00 - 12:00)
    delivery_date DATETIME,
    note NVARCHAR(MAX),
    updated_at DATETIME,
    CONSTRAINT FK_Deliveries_Orders FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE
);