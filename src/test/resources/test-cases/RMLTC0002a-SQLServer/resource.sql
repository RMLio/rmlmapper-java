USE TestDB;
EXEC sp_msforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT all'
EXEC sp_msforeachtable 'DROP TABLE ?'

CREATE TABLE student (
  "ID" INTEGER,
  "Name" VARCHAR(50)
);
INSERT INTO student ("ID", "Name") values (10, 'Venus');

