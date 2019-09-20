CREATE TABLE Customers(
	customerNumber INTEGER,
	customerName VARCHAR(50),
	contactLastName VARCHAR(50),
	contactFirstName VARCHAR(50),
	phone VARCHAR(50),
	addressLine1 VARCHAR(50),
	addressLine2 VARCHAR(50),
	city VARCHAR(50),
	state VARCHAR(50),
	postalCode VARCHAR(15),
	country VARCHAR(50),
	salesRepEmployeeNumber INTEGER,
	creditLimit DOUBLE
);

CREATE UNIQUE INDEX customers_pk ON Customers( customerNumber );

CREATE TABLE Employees(
	employeeNumber INTEGER,
	lastName VARCHAR(50),
	firstName VARCHAR(50),
	extension VARCHAR(10),
	email VARCHAR(100),
	officeCode VARCHAR(10),
	reportsTo INTEGER,
	jobTitle VARCHAR(50) 
);

CREATE UNIQUE INDEX employees_pk ON Employees( employeeNumber );

CREATE TABLE Offices(
	officeCode VARCHAR(10),
	city VARCHAR(50),
	phone VARCHAR(50),
	addressLine1 VARCHAR(50),
	addressLine2 VARCHAR(50),
	state VARCHAR(50),
	country VARCHAR(50),
	postalCode VARCHAR(15),
	territory VARCHAR(10)
);

CREATE UNIQUE INDEX offices_pk ON Offices ( officeCode );

CREATE TABLE Products(
	productCode VARCHAR(15),
	productName VARCHAR(70),
	productLine VARCHAR(50),
	productScale VARCHAR(10),
	productVendor VARCHAR(50),
	productDescription VARCHAR(4000),
	quantityInStock INTEGER,
	buyPrice DOUBLE,
	MSRP DOUBLE
);
CREATE UNIQUE INDEX products_pk ON Products( productCode );

CREATE TABLE ProductLines(
	productLine VARCHAR(50),
	textDescription VARCHAR(4000),
	htmlDescription VARCHAR(4000),
	image BLOB
);
CREATE UNIQUE INDEX productLines_pk on ProductLines( productLine );

CREATE TABLE Orders(
	orderNumber INTEGER,
	orderDate DATE,
	requiredDate DATE,
	shippedDate DATE,
	status VARCHAR(15),
	comments VARCHAR(4000),
	customerNumber INTEGER 
);
CREATE UNIQUE INDEX orders_pk ON Orders( orderNumber );
CREATE INDEX orders_cutomer ON Orders( customerNumber );

CREATE TABLE OrderDetails(
	orderNumber INTEGER,
	productCode VARCHAR(15),
	quantityOrdered INTEGER,
	priceEach DOUBLE,
	orderLineNumber SMALLINT);
CREATE UNIQUE INDEX orderDetails_pk ON OrderDetails( orderNumber, productCode );

CREATE TABLE Payments(
	customerNumber INTEGER,
	checkNumber VARCHAR(50),
	paymentDate DATE,
	amount DOUBLE 
);
CREATE UNIQUE INDEX payments_pk ON Payments( customerNumber, checkNumber );

CREATE TABLE department(
	id INTEGER,
	name VARCHAR(200)
);

CREATE TABLE BinaryData(
	id INTEGER,
	data BLOB
);



