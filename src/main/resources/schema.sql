create table BOOK (
  ID int not null AUTO_INCREMENT,
  TITLE varchar2(100) not null,
  GENRE varchar2(100) not null,
  CREATED_ON timestamp,
  LAST_UPDATE_ON timestamp,
  PRIMARY KEY ( ID )
);