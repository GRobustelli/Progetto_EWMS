CREATE SCHEMA ewmsDB;

use ewmsDB;

create table Utente (
matricola char(13) primary key not null,
email varchar(250) character set utf8mb4 unique not null,
nome varchar (50) not null,
cognome varchar(50) not null,
dataDiNascita date not null,
hashPassword varchar(50) not null,
newUtente boolean not null default true,
ruolo varchar(20) not null
);

create table Supervisore(
matricola char(13) primary key not null,
foreign key (matricola) references Utente(matricola) on update cascade on delete cascade
);

create table Dipendente(
matricola char(13) primary key not null,
supMatricola char(13) not null,
foreign key (matricola) references Utente(matricola) on update cascade on delete cascade,
foreign key (supMatricola) references Utente(matricola) on update cascade
);

create table Task(
id bigint auto_increment primary key not null,
titolo varchar(50) not null,
dataDiScadenza date not null,
dataDiCreazione date not null,
priorita varchar(20) not null,
istruzioni varchar(2000) not null,
stato varchar(20) not null,
supervisore char(13) not null,
dipendente char(13) not null,
foreign key (supervisore) references Utente(matricola) on update cascade on delete cascade,
foreign key (dipendente) references Utente(matricola) on update cascade on delete cascade
);


create table Allegato(
filename varchar(100) primary key not null,
task_id bigint not null,
filepath varchar(200) not null,
contentType varchar(20) not null,
foreign key (task_id) references Task(id) on update cascade on delete cascade
);

create table Notifica(
id bigint auto_increment not null primary key,
task_id bigint not null,
messaggio varchar(500),
sender char(13) not null,
receiver char(13) not null,
vista boolean not null default false,
foreign key (task_id) references Task(id) on update cascade on delete cascade,
foreign key (sender) references Utente(matricola) on update cascade on delete cascade,
foreign key (receiver) references Utente(matricola) on update cascade on delete cascade
);


drop schema ewmsdb;