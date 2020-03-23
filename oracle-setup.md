# How to configure Oracle DB for tests

1. Connect to the database via `sqlplus sys/test@//193.190.127.196:1521/XE as sysdba`
2. Execute `alter session set "_ORACLE_SCRIPT"=true;` to allow the creation of new users.
3. Create a new user via `CREATE USER rmlmapper_test IDENTIFIED BY test;`
4. Allow the user to connect via `GRANT CONNECT TO rmlmapper_test;`
5. Allow the user to create a session via `GRANT CREATE SESSION TO rmlmapper_test;`
6. Allow the user to create tables via `grant create table to rmlmapper_test;`
7. Allow the user to add data via `alter user rmlmapper_test quota unlimited on users;`