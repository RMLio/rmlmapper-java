DROP TABLE IF EXISTS Student_Sport cascade; -- normally this is dropped in case 18, but case 18 is commented out due to failure.
DROP TABLE IF EXISTS Student cascade;
CREATE TABLE Student (Name VARCHAR(50));
INSERT INTO Student (Name) VALUES ('http://company.com/Alice');
INSERT INTO Student (Name) VALUES ('Bob');
INSERT INTO Student (Name) VALUES ('Bob/Charles');
INSERT INTO Student (Name) VALUES ('path/../Danny');
INSERT INTO Student (Name) VALUES ('Emily Smith');