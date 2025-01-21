--create database reveng;
--grant all on reveng.* to reveng@localhost identified by 'reveng';
--mysql -u reveng -preveng <reveng.sql

use reveng;

DROP TABLE IF EXISTS thing;
DROP TABLE IF EXISTS other;
DROP TABLE IF EXISTS compound_unique;
DROP TABLE IF EXISTS compos;
DROP TABLE IF EXISTS track_playlists;
DROP TABLE IF EXISTS track;
DROP TABLE IF EXISTS playlist;
DROP TABLE IF EXISTS mediatype;
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS album;
DROP TABLE IF EXISTS artist;
DROP TABLE IF EXISTS invoiceline;
DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS visit;
DROP TABLE IF EXISTS library;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS author_books;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS author;

CREATE TABLE author (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  version bigint(20) NOT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE book (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  version bigint(20) NOT NULL,
  title varchar(255) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE author_books (
  author_id bigint(20) NOT NULL,
  book_id bigint(20) NOT NULL,
  PRIMARY KEY (author_id,book_id),
  KEY FK24C812F6183CFE1B (book_id),
  KEY FK24C812F6DAE0A69B (author_id),
  CONSTRAINT FK24C812F6183CFE1B FOREIGN KEY (book_id) REFERENCES book (id),
  CONSTRAINT FK24C812F6DAE0A69B FOREIGN KEY (author_id) REFERENCES author (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--------------------------

CREATE TABLE user (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  version bigint(20) NOT NULL,
  account_expired bit(1) NOT NULL,
  account_locked bit(1) NOT NULL,
  enabled bit(1) NOT NULL,
  password varchar(255) NOT NULL,
  password_expired bit(1) NOT NULL,
  username varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE role (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  version bigint(20) NOT NULL,
  authority varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY authority (authority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_role (
  role_id bigint(20) NOT NULL,
  user_id bigint(20) NOT NULL,
  date_updated datetime NOT NULL,
  PRIMARY KEY (role_id,user_id),
  KEY FK143BF46A667AF6FB (role_id),
  KEY FK143BF46ABA5BADB (user_id),
  CONSTRAINT FK143BF46A667AF6FB FOREIGN KEY (role_id) REFERENCES role (id),
  CONSTRAINT FK143BF46ABA5BADB FOREIGN KEY (user_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--------------------------

CREATE TABLE library (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  version bigint(20) NOT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE visit (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  library_id bigint(20) NOT NULL,
  person varchar(255) NOT NULL,
  visit_date datetime NOT NULL,
  PRIMARY KEY (id),
  KEY FK6B04D4BE8E8E739 (library_id),
  CONSTRAINT FK6B04D4BE8E8E739 FOREIGN KEY (library_id) REFERENCES library (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--------------------------

CREATE TABLE employee (
  employeeid decimal(19,2) NOT NULL,
  address varchar(70) DEFAULT NULL,
  birthdate datetime DEFAULT NULL,
  city varchar(40) DEFAULT NULL,
  country varchar(40) DEFAULT NULL,
  email varchar(60) DEFAULT NULL,
  employee_id decimal(19,2) DEFAULT NULL,
  fax varchar(24) DEFAULT NULL,
  firstname varchar(20) NOT NULL,
  hiredate datetime DEFAULT NULL,
  lastname varchar(20) NOT NULL,
  phone varchar(24) DEFAULT NULL,
  postalcode varchar(10) DEFAULT NULL,
  state varchar(40) DEFAULT NULL,
  title varchar(30) DEFAULT NULL,
  PRIMARY KEY (employeeid),
  KEY FK4722E6AEF3918934 (employee_id),
  CONSTRAINT FK4722E6AEF3918934 FOREIGN KEY (employee_id) REFERENCES employee (employeeid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE customer (
  customerid decimal(19,2) NOT NULL,
  address varchar(70) DEFAULT NULL,
  city varchar(40) DEFAULT NULL,
  company varchar(80) DEFAULT NULL,
  country varchar(40) DEFAULT NULL,
  email varchar(60) NOT NULL,
  employee_id decimal(19,2) NOT NULL,
  fax varchar(24) DEFAULT NULL,
  firstname varchar(40) NOT NULL,
  lastname varchar(20) NOT NULL,
  phone varchar(24) DEFAULT NULL,
  postalcode varchar(10) DEFAULT NULL,
  state varchar(40) DEFAULT NULL,
  PRIMARY KEY (customerid),
  KEY FK24217FDEF3918934 (employee_id),
  CONSTRAINT FK24217FDEF3918934 FOREIGN KEY (employee_id) REFERENCES employee (employeeid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE invoice (
  invoiceid decimal(19,2) NOT NULL,
  billingaddress varchar(70) DEFAULT NULL,
  billingcity varchar(40) DEFAULT NULL,
  billingcountry varchar(40) DEFAULT NULL,
  billingpostalcode varchar(10) DEFAULT NULL,
  billingstate varchar(40) DEFAULT NULL,
  customer_id decimal(19,2) NOT NULL,
  invoicedate datetime NOT NULL,
  total decimal(19,2) NOT NULL,
  PRIMARY KEY (invoiceid),
  KEY FK74D6432D3074BB34 (customer_id),
  CONSTRAINT FK74D6432D3074BB34 FOREIGN KEY (customer_id) REFERENCES customer (customerid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE invoiceline (
  invoicelineid decimal(19,2) NOT NULL,
  invoice_id decimal(19,2) NOT NULL,
  quantity decimal(19,2) NOT NULL,
  unitprice decimal(19,2) NOT NULL,
  PRIMARY KEY (invoicelineid),
  KEY FKCCA994A1B0CA9920 (invoice_id),
  CONSTRAINT FKCCA994A1B0CA9920 FOREIGN KEY (invoice_id) REFERENCES invoice (invoiceid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--------------------------

CREATE TABLE artist (
  artistid decimal(19,2) NOT NULL,
  name varchar(120) DEFAULT NULL,
  PRIMARY KEY (artistid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE album (
  albumid decimal(19,2) NOT NULL,
  artist_id decimal(19,2) NOT NULL,
  title varchar(160) NOT NULL,
  PRIMARY KEY (albumid),
  KEY FK5897E6F48625D14 (artist_id),
  CONSTRAINT FK5897E6F48625D14 FOREIGN KEY (artist_id) REFERENCES artist (artistid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE genre (
  genreid decimal(19,2) NOT NULL,
  name varchar(120) DEFAULT NULL,
  PRIMARY KEY (genreid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE mediatype (
  mediatypeid decimal(19,2) NOT NULL,
  name varchar(120) DEFAULT NULL,
  PRIMARY KEY (mediatypeid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE playlist (
  playlistid decimal(19,2) NOT NULL,
  name varchar(120) DEFAULT NULL,
  PRIMARY KEY (playlistid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE track (
  trackid decimal(19,2) NOT NULL,
  album_id decimal(19,2) NOT NULL,
  bytes decimal(19,2) DEFAULT NULL,
  composer varchar(220) DEFAULT NULL,
  genre_id decimal(19,2) NOT NULL,
  mediatype_id decimal(19,2) NOT NULL,
  milliseconds decimal(19,2) NOT NULL,
  name varchar(200) NOT NULL,
  unitprice decimal(19,2) NOT NULL,
  PRIMARY KEY (trackid),
  KEY FK697F14B3320FE00 (mediatype_id),
  KEY FK697F14B6E22DAE0 (genre_id),
  KEY FK697F14B5C89A360 (album_id),
  CONSTRAINT FK697F14B3320FE00 FOREIGN KEY (mediatype_id) REFERENCES mediatype (mediatypeid),
  CONSTRAINT FK697F14B5C89A360 FOREIGN KEY (album_id) REFERENCES album (albumid),
  CONSTRAINT FK697F14B6E22DAE0 FOREIGN KEY (genre_id) REFERENCES genre (genreid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE track_playlists (
  playlist_id decimal(19,2) NOT NULL,
  track_id decimal(19,2) NOT NULL,
  PRIMARY KEY (track_id,playlist_id),
  KEY FK1929C2ED6B3806B4 (playlist_id),
  KEY FK1929C2ED4E0065E0 (track_id),
  CONSTRAINT FK1929C2ED4E0065E0 FOREIGN KEY (track_id) REFERENCES track (trackid),
  CONSTRAINT FK1929C2ED6B3806B4 FOREIGN KEY (playlist_id) REFERENCES playlist (playlistid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--------------------------

CREATE TABLE compos (
  first_name varchar(255) NOT NULL,
  last_name varchar(255) NOT NULL,
  version bigint(20) NOT NULL,
  other varchar(255) NOT NULL,
  PRIMARY KEY (first_name,last_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE compound_unique (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  version bigint(20) NOT NULL,
  prop1 varchar(255) NOT NULL,
  prop2 varchar(255) NOT NULL,
  prop3 varchar(255) NOT NULL,
  prop4 varchar(255) NOT NULL,
  prop5 varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY prop4 (prop4,prop3,prop2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE other (
  username varchar(255) NOT NULL,
  nonstandard_version_name bigint(20) NOT NULL,
  PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE thing (
  thing_id bigint(20) NOT NULL AUTO_INCREMENT,
  version bigint(20) NOT NULL,
  email varchar(255) NOT NULL,
  float_value float NOT NULL,
  name varchar(123) DEFAULT NULL,
  new_column bit(1) DEFAULT NULL,
  PRIMARY KEY (thing_id),
  UNIQUE KEY email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
