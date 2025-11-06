-- =============================================
-- Script: Seed dữ liệu dựa trên 3 user accounts đã tạo
-- DB: CoffeeStoreDB
-- Users:
--   1. admin@gmail.com (Customer) - b51f4fa3-fa2c-4ee5-ac11-312e906e89c3
--   2. test@gmail.com (Barista) - 4df4ecac-1008-45e9-a3f6-aa0c8f687549
--   3. bao@gmail.com (Customer) - c4b3455a-53c0-4d9b-b0fe-c828cd27acbd
-- =============================================

USE CoffeeStoreDB;
GO

SET NOCOUNT ON;

BEGIN TRAN;

-- Khai báo User IDs từ accounts đã tạo
DECLARE @AdminUserId UNIQUEIDENTIFIER = 'b51f4fa3-fa2c-4ee5-ac11-312e906e89c3';  -- admin@gmail.com (Customer)
DECLARE @BaristaUserId UNIQUEIDENTIFIER = '4df4ecac-1008-45e9-a3f6-aa0c8f687549';  -- test@gmail.com (Barista)
DECLARE @BaoUserId UNIQUEIDENTIFIER = 'c4b3455a-53c0-4d9b-b0fe-c828cd27acbd';  -- bao@gmail.com (Customer)

-- =============================================
-- 1. CATEGORIES
-- =============================================
DECLARE @CategoryHot UNIQUEIDENTIFIER = NEWID();
DECLARE @CategoryCold UNIQUEIDENTIFIER = NEWID();

INSERT dbo.Category (Id, Name, Description, CreatedDate)
VALUES
(@CategoryHot,  N'Cà phê nóng', N'Các loại cà phê phục vụ nóng', SYSDATETIME()),
(@CategoryCold, N'Cà phê lạnh', N'Các loại cà phê phục vụ lạnh', SYSDATETIME());

-- =============================================
-- 2. PRODUCTS (với hình ảnh từ Unsplash)
-- =============================================
DECLARE @ProdEspresso UNIQUEIDENTIFIER = NEWID();
DECLARE @ProdLatte    UNIQUEIDENTIFIER = NEWID();
DECLARE @ProdCappuccino UNIQUEIDENTIFIER = NEWID();
DECLARE @ProdColdBrew UNIQUEIDENTIFIER = NEWID();
DECLARE @ProdMocha    UNIQUEIDENTIFIER = NEWID();
DECLARE @ProdAmericano UNIQUEIDENTIFIER = NEWID();

INSERT dbo.Products (Id, Name, Description, Origin, RoastLevel, BrewMethod, ImageUrl, IsActive, CreatedAt, UpdatedAt, CategoryId)
VALUES
(@ProdEspresso, N'Espresso', N'Espresso truyền thống, đậm đà và mạnh mẽ', N'Vietnam', 'Medium', 'Espresso', 
 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=800&h=600&fit=crop', 1, SYSDATETIME(), SYSDATETIME(), @CategoryHot),

(@ProdLatte, N'Latte', N'Cà phê sữa Ý, mềm mại và béo ngậy', N'Brazil', 'Light', 'Espresso', 
 'https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=800&h=600&fit=crop', 1, SYSDATETIME(), SYSDATETIME(), @CategoryHot),

(@ProdCappuccino, N'Cappuccino', N'Cappuccino với lớp bọt sữa dày', N'Colombia', 'Medium', 'Espresso', 
 'https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=800&h=600&fit=crop', 1, SYSDATETIME(), SYSDATETIME(), @CategoryHot),

(@ProdColdBrew, N'Cold Brew', N'Cà phê ủ lạnh 12 giờ, mát lạnh và tinh tế', N'Kenya', 'Dark', 'ColdBrew', 
 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=800&h=600&fit=crop', 1, SYSDATETIME(), SYSDATETIME(), @CategoryCold),

(@ProdMocha, N'Mocha', N'Cà phê pha với sô cô la, ngọt ngào và đậm đà', N'Ethiopia', 'Medium', 'Espresso', 
 'https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?w=800&h=600&fit=crop', 1, SYSDATETIME(), SYSDATETIME(), @CategoryHot),

(@ProdAmericano, N'Americano', N'Espresso pha với nước nóng, nhẹ nhàng', N'Vietnam', 'Medium', 'Espresso', 
 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&h=600&fit=crop', 1, SYSDATETIME(), SYSDATETIME(), @CategoryHot);

-- =============================================
-- 3. COFFEE VARIANTS (Size: 1=S, 2=M, 3=L)
-- =============================================
DECLARE @VarEspS UNIQUEIDENTIFIER = NEWID();
DECLARE @VarEspM UNIQUEIDENTIFIER = NEWID();
DECLARE @VarEspL UNIQUEIDENTIFIER = NEWID();
DECLARE @VarLatM UNIQUEIDENTIFIER = NEWID();
DECLARE @VarLatL UNIQUEIDENTIFIER = NEWID();
DECLARE @VarCapM UNIQUEIDENTIFIER = NEWID();
DECLARE @VarCapL UNIQUEIDENTIFIER = NEWID();
DECLARE @VarColdM UNIQUEIDENTIFIER = NEWID();
DECLARE @VarColdL UNIQUEIDENTIFIER = NEWID();
DECLARE @VarMochaM UNIQUEIDENTIFIER = NEWID();
DECLARE @VarMochaL UNIQUEIDENTIFIER = NEWID();
DECLARE @VarAmerM UNIQUEIDENTIFIER = NEWID();

INSERT dbo.CoffeeVariants (Id, ProductId, Size, BasePrice, IsActive, CreatedAt)
VALUES
(@VarEspS,  @ProdEspresso, 1 /*S*/, 30000, 1, SYSDATETIME()),
(@VarEspM,  @ProdEspresso, 2 /*M*/, 35000, 1, SYSDATETIME()),
(@VarEspL,  @ProdEspresso, 3 /*L*/, 40000, 1, SYSDATETIME()),
(@VarLatM,  @ProdLatte,    2 /*M*/, 45000, 1, SYSDATETIME()),
(@VarLatL,  @ProdLatte,    3 /*L*/, 50000, 1, SYSDATETIME()),
(@VarCapM,  @ProdCappuccino, 2 /*M*/, 42000, 1, SYSDATETIME()),
(@VarCapL,  @ProdCappuccino, 3 /*L*/, 47000, 1, SYSDATETIME()),
(@VarColdM, @ProdColdBrew, 2 /*M*/, 55000, 1, SYSDATETIME()),
(@VarColdL, @ProdColdBrew, 3 /*L*/, 60000, 1, SYSDATETIME()),
(@VarMochaM, @ProdMocha,   2 /*M*/, 48000, 1, SYSDATETIME()),
(@VarMochaL, @ProdMocha,   3 /*L*/, 53000, 1, SYSDATETIME()),
(@VarAmerM, @ProdAmericano, 2 /*M*/, 38000, 1, SYSDATETIME());

-- =============================================
-- 4. COFFEE ADDONS
-- =============================================
DECLARE @AddonShot  UNIQUEIDENTIFIER = NEWID();
DECLARE @AddonSyrup UNIQUEIDENTIFIER = NEWID();
DECLARE @AddonWhippedCream UNIQUEIDENTIFIER = NEWID();
DECLARE @AddonCaramel UNIQUEIDENTIFIER = NEWID();
DECLARE @AddonCoconut UNIQUEIDENTIFIER = NEWID();

INSERT dbo.CoffeeAddons (Id, Name, Price, IsActive, CreatedAt, UpdatedAt)
VALUES
(@AddonShot,  N'Extra Shot',        10000, 1, SYSDATETIME(), SYSDATETIME()),
(@AddonSyrup, N'Vanilla Syrup',      7000, 1, SYSDATETIME(), SYSDATETIME()),
(@AddonWhippedCream, N'Whipped Cream', 8000, 1, SYSDATETIME(), SYSDATETIME()),
(@AddonCaramel, N'Caramel Drizzle',  9000, 1, SYSDATETIME(), SYSDATETIME()),
(@AddonCoconut, N'Coconut Milk',     6000, 1, SYSDATETIME(), SYSDATETIME());

-- =============================================
-- 5. ORDERS (cho các Customer)
-- =============================================
DECLARE @Order1 BIGINT, @Order2 BIGINT, @Order3 BIGINT, @Order4 BIGINT;

-- Order 1: admin@gmail.com - PROCESSING, UNPAID
INSERT dbo.Orders (OrderDate, TotalAmount, Status, PaymentStatus, CreatedAt, UpdatedAt, CustomerId, DeliveryType, OrderCode)
VALUES (DATEADD(MINUTE,-30,SYSDATETIME()), 0, N'PROCESSING', N'UNPAID', SYSDATETIME(), SYSDATETIME(), @AdminUserId, N'DineIn', 100001);
SET @Order1 = SCOPE_IDENTITY();

-- Order 2: bao@gmail.com - CONFIRMED, PAID
INSERT dbo.Orders (OrderDate, TotalAmount, Status, PaymentStatus, CreatedAt, UpdatedAt, CustomerId, DeliveryType, OrderCode)
VALUES (DATEADD(HOUR,-1,SYSDATETIME()), 0, N'CONFIRMED', N'PAID', SYSDATETIME(), SYSDATETIME(), @BaoUserId, N'TakeAway', 100002);
SET @Order2 = SCOPE_IDENTITY();

-- Order 3: admin@gmail.com - COMPLETED, PAID
INSERT dbo.Orders (OrderDate, TotalAmount, Status, PaymentStatus, CreatedAt, UpdatedAt, CustomerId, DeliveryType, OrderCode)
VALUES (DATEADD(HOUR,-2,SYSDATETIME()), 0, N'COMPLETED', N'PAID', SYSDATETIME(), SYSDATETIME(), @AdminUserId, N'DineIn', 100003);
SET @Order3 = SCOPE_IDENTITY();

-- Order 4: bao@gmail.com - SHIPPING, PAID
INSERT dbo.Orders (OrderDate, TotalAmount, Status, PaymentStatus, CreatedAt, UpdatedAt, CustomerId, DeliveryType, OrderCode)
VALUES (DATEADD(MINUTE,-15,SYSDATETIME()), 0, N'SHIPPING', N'PAID', SYSDATETIME(), SYSDATETIME(), @BaoUserId, N'Delivery', 100004);
SET @Order4 = SCOPE_IDENTITY();

-- =============================================
-- 6. ORDER DETAILS
-- =============================================
DECLARE @Od1 UNIQUEIDENTIFIER = NEWID();
DECLARE @Od2 UNIQUEIDENTIFIER = NEWID();
DECLARE @Od3 UNIQUEIDENTIFIER = NEWID();
DECLARE @Od4 UNIQUEIDENTIFIER = NEWID();
DECLARE @Od5 UNIQUEIDENTIFIER = NEWID();
DECLARE @Od6 UNIQUEIDENTIFIER = NEWID();
DECLARE @Od7 UNIQUEIDENTIFIER = NEWID();

-- Order 1: Espresso M x2, Latte L x1
INSERT dbo.OrderDetails (Id, OrderId, VariantId, Quantity, UnitPrice, Notes, Temperature, Sweetness, MilkType)
VALUES
(@Od1, @Order1, @VarEspM,  2, 35000, N'Ít đá',  0 /*Hot*/, 1 /*Normal*/, 0 /*Dairy*/),
(@Od2, @Order1, @VarLatL,  1, 50000, N'Ít ngọt',2 /*Ice*/,  2 /*Less*/,   0 /*Dairy*/);

-- Order 2: Cold Brew M x1, Cappuccino M x1
INSERT dbo.OrderDetails (Id, OrderId, VariantId, Quantity, UnitPrice, Notes, Temperature, Sweetness, MilkType)
VALUES
(@Od3, @Order2, @VarColdM, 1, 55000, NULL,      1 /*ColdBrew*/, 1 /*Normal*/, 3 /*None*/),
(@Od4, @Order2, @VarCapM,  1, 42000, N'Nóng',  0 /*Hot*/, 1 /*Normal*/, 0 /*Dairy*/);

-- Order 3: Mocha M x2
INSERT dbo.OrderDetails (Id, OrderId, VariantId, Quantity, UnitPrice, Notes, Temperature, Sweetness, MilkType)
VALUES
(@Od5, @Order3, @VarMochaM, 2, 48000, NULL,     0 /*Hot*/, 0 /*Sweet*/,  0 /*Dairy*/);

-- Order 4: Americano M x1, Espresso L x1
INSERT dbo.OrderDetails (Id, OrderId, VariantId, Quantity, UnitPrice, Notes, Temperature, Sweetness, MilkType)
VALUES
(@Od6, @Order4, @VarAmerM, 1, 38000, N'Nóng vừa', 0 /*Hot*/, 1 /*Normal*/, 0 /*Dairy*/),
(@Od7, @Order4, @VarEspL,  1, 40000, NULL,        0 /*Hot*/, 1 /*Normal*/, 0 /*Dairy*/);

-- =============================================
-- 7. ORDER ITEM ADDONS
-- =============================================
INSERT dbo.OrderItemAddons (Id, OrderDetailId, AddonId, Price)
VALUES
(NEWID(), @Od1, @AddonShot,  10000),
(NEWID(), @Od2, @AddonSyrup,  7000),
(NEWID(), @Od4, @AddonWhippedCream, 8000),
(NEWID(), @Od5, @AddonCaramel, 9000),
(NEWID(), @Od5, @AddonShot, 10000),
(NEWID(), @Od6, @AddonCoconut, 6000);

-- =============================================
-- 8. TÍNH LẠI TOTAL AMOUNT CHO ORDERS
-- =============================================
;WITH S AS (
  SELECT OrderId, SUM(Quantity * UnitPrice) AS ItemsTotal
  FROM dbo.OrderDetails
  GROUP BY OrderId
),
A AS (
  SELECT od.OrderId, SUM(oia.Price) AS AddonsTotal
  FROM dbo.OrderItemAddons oia
  JOIN dbo.OrderDetails od ON od.Id = oia.OrderDetailId
  GROUP BY od.OrderId
)
UPDATE o
SET o.TotalAmount = ISNULL(S.ItemsTotal,0) + ISNULL(A.AddonsTotal,0)
FROM dbo.Orders o
LEFT JOIN S ON S.OrderId = o.Id
LEFT JOIN A ON A.OrderId = o.Id
WHERE o.Id IN (@Order1, @Order2, @Order3, @Order4);

-- =============================================
-- 9. PAYMENTS (cho các order đã thanh toán)
-- =============================================
DECLARE @PaymentAmount DECIMAL(18,2);

-- Payment cho Order 2
SET @PaymentAmount = (SELECT TotalAmount FROM dbo.Orders WHERE Id = @Order2);
IF @PaymentAmount IS NOT NULL
BEGIN
    INSERT dbo.Payments (Id, Method, Status, PaymentDate, Amount, OrderId, CreatedAt, UpdatedAt)
    VALUES (NEWID(), N'Cash', N'Success', SYSDATETIME(), @PaymentAmount, @Order2, SYSDATETIME(), SYSDATETIME());
END

-- Payment cho Order 3
SET @PaymentAmount = (SELECT TotalAmount FROM dbo.Orders WHERE Id = @Order3);
IF @PaymentAmount IS NOT NULL
BEGIN
    INSERT dbo.Payments (Id, Method, Status, PaymentDate, Amount, OrderId, CreatedAt, UpdatedAt)
    VALUES (NEWID(), N'Card', N'Success', SYSDATETIME(), @PaymentAmount, @Order3, SYSDATETIME(), SYSDATETIME());
END

-- Payment cho Order 4
SET @PaymentAmount = (SELECT TotalAmount FROM dbo.Orders WHERE Id = @Order4);
IF @PaymentAmount IS NOT NULL
BEGIN
    INSERT dbo.Payments (Id, Method, Status, PaymentDate, Amount, OrderId, CreatedAt, UpdatedAt)
    VALUES (NEWID(), N'Card', N'Success', SYSDATETIME(), @PaymentAmount, @Order4, SYSDATETIME(), SYSDATETIME());
END

COMMIT TRAN;

-- =============================================
-- KIỂM TRA KẾT QUẢ
-- =============================================
DECLARE @CatCount INT, @ProdCount INT, @VarCount INT, @AddonCount INT, @OrderCount INT, @DetailCount INT, @PaymentCount INT;

SELECT @CatCount = COUNT(*) FROM dbo.Category;
SELECT @ProdCount = COUNT(*) FROM dbo.Products;
SELECT @VarCount = COUNT(*) FROM dbo.CoffeeVariants;
SELECT @AddonCount = COUNT(*) FROM dbo.CoffeeAddons;
SELECT @OrderCount = COUNT(*) FROM dbo.Orders;
SELECT @DetailCount = COUNT(*) FROM dbo.OrderDetails;
SELECT @PaymentCount = COUNT(*) FROM dbo.Payments;

PRINT '';
PRINT '=== SUMMARY ===';
PRINT 'Categories: ' + CAST(@CatCount AS VARCHAR);
PRINT 'Products: ' + CAST(@ProdCount AS VARCHAR);
PRINT 'Variants: ' + CAST(@VarCount AS VARCHAR);
PRINT 'Addons: ' + CAST(@AddonCount AS VARCHAR);
PRINT 'Orders: ' + CAST(@OrderCount AS VARCHAR);
PRINT 'OrderDetails: ' + CAST(@DetailCount AS VARCHAR);
PRINT 'Payments: ' + CAST(@PaymentCount AS VARCHAR);
PRINT '';
PRINT '=== ORDERS BY USER ===';
SELECT 
    u.Email,
    u.FullName,
    o.Id AS OrderId,
    o.OrderDate,
    o.TotalAmount,
    o.Status,
    o.PaymentStatus,
    o.OrderCode
FROM dbo.Orders o
JOIN dbo.Users u ON u.Id = o.CustomerId
ORDER BY o.OrderDate DESC;
PRINT '';
PRINT '=== PRODUCTS WITH IMAGES ===';
SELECT Id, Name, ImageUrl FROM dbo.Products WHERE ImageUrl IS NOT NULL;
PRINT '';
PRINT 'Script hoàn tất thành công!';
GO
