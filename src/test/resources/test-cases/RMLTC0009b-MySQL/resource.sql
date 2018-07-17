USE test;
DROP TABLE IF EXISTS test.student;

CREATE TABLE student (
  ID INTEGER,
  Sport VARCHAR(50),
  Name VARCHAR(50)
);
INSERT INTO student values ('10', '100', 'Venus Williams');
INSERT INTO student values ('20', NULL, 'Demi Moore');

DROP TABLE IF EXISTS test.sport;

CREATE TABLE sport (
  ID INTEGER,
  Name VARCHAR(50)
);
INSERT INTO sport values ('100', 'Tennis');
