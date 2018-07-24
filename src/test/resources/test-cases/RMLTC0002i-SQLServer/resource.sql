rr:subjectMap [
          rr:template "http://example.com/{ID}/{Name}";
        ];

        rr:predicateObjectMap [
          rr:predicate ex:id ;
          rr:objectMap [ rml:reference "IDs" ]
        ].USE TestDB;
DROP TABLE IF EXISTS student;
CREATE TABLE student (
  "ID" INTEGER,
  "Name" VARCHAR(50)
);
INSERT INTO student values ('10', 'Venus');
