USE test;
DROP TABLE IF EXISTS test.persons;

CREATE TABLE persons (
  fname VARCHAR(200),
  lname VARCHAR(200),
  amount INTEGER
);
INSERT INTO persons values ('Bob','Smith','30');
INSERT INTO persons values ('Sue','Jones','20');
INSERT INTO persons values ('Bob','Smith','30');
