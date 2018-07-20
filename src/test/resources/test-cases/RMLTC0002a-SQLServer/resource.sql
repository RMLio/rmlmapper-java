USE TestDB;
DROP TABLE IF EXISTS student;
CREATE TABLE student (
  "ID" INTEGER,
  "Name" VARCHAR(50)
);
INSERT INTO student ("ID", "Name") values (10, 'Venus');

