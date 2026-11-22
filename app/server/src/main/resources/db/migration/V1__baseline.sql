-- Nền tối thiểu. Schema chính thức do kiến trúc sư cấp, thêm từ V10 trở lên.
CREATE TABLE health_probe (
    id     smallint PRIMARY KEY CHECK (id = 1),
    status text NOT NULL
);
INSERT INTO health_probe (id, status) VALUES (1, 'ok');
