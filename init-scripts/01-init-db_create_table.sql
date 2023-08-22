-- CREATE TABLE SCRIPTS
-- ====================
CREATE DATABASE lineman;
\c lineman;

DROP TABLE IF EXISTS restaurant_detail;
CREATE TABLE IF NOT EXISTS restaurant_detail (
    id VARCHAR(20) PRIMARY KEY,
    restaurant_name TEXT,
    category TEXT,
    estimated_cooking_time NUMERIC(5, 1),
    latitude DECIMAL,
    longitude DECIMAL
);

DROP TABLE IF EXISTS order_detail;
CREATE TABLE IF NOT EXISTS order_detail (
    order_created_timestamp TIMESTAMP,
    status VARCHAR(30),
    price INTEGER,
    discount DECIMAL,
    id UUID,
    driver_id UUID,
    user_id UUID,
    restaurant_id VARCHAR(20)
);

COPY restaurant_detail (id, restaurant_name, category, estimated_cooking_time, latitude, longitude) FROM '/csv-files/restaurant_detail.csv' DELIMITER ',' CSV HEADER;

COPY order_detail (order_created_timestamp, status, price, discount, id, driver_id, user_id, restaurant_id) FROM '/csv-files/order_detail.csv' DELIMITER ',' CSV HEADER;
