CREATE SCHEMA ewmsDB;
use ewmsDB;
create table Utente (
email varchar(250) character set utf8mb4 primary key not null,
matricola char(13) unique not null,
nome varchar (50) not null,
cognome varchar(50) not null,
dataDiNascita date not null,
hashPassword varchar(50) not null,
newUtente boolean not null default true,
ruolo varchar(20) not null
);

create table Supervisore(
email varchar(250) character set utf8mb4 primary key not null,
foreign key (email) references Utente(email) on update cascade on delete cascade
);

create table Dipendente(
email varchar(250) character set utf8mb4 primary key not null,
supEmail varchar(250) character set utf8mb4 not null,
foreign key (email) references Utente(email) on update cascade on delete cascade,
foreign key (supEmail) references Utente(email) on update cascade
);

create table GestoreAccount(
email varchar(250) character set utf8mb4 primary key not null,
foreign key (email) references Utente(email) on update cascade on delete cascade
);


create table Task(
id bigint auto_increment primary key not null,
titolo varchar(50) not null,
dataDiScadenza date not null,
dataDiCreazione date not null,
-- Priorita varchar(20) not null,
istruzioni varchar(2000) not null,
stato varchar(20) not null,
supervisore varchar(250) character set utf8mb4 not null,
dipendente varchar(250) character set utf8mb4 not null,
foreign key (supervisore) references Utente(email) on update cascade on delete cascade,
foreign key (dipendente) references Utente(email) on update cascade on delete cascade
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
sender varchar(250) character set utf8mb4 not null,
receiver varchar(250) character set utf8mb4 not null,
foreign key (task_id) references Task(id) on update cascade on delete cascade,
foreign key (sender) references Utente(email) on update cascade on delete cascade,
foreign key (receiver) references Utente(email) on update cascade on delete cascade
);