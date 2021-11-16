USE test;
DROP TABLE IF EXISTS test.student;
CREATE TABLE student (
  ID INTEGER,
  Name VARCHAR(50)
);
INSERT INTO student values (10, 'Venus');
INSERT INTO student values (11, NULL);
INSERT INTO student values (12, 'Serena');