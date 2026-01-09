CREATE SCHEMA ewms;

create table Utente (
email nvarchar(250) primary key not null,
matricola char(13) unique not null,
nome varchar (50) not null,
cognome varchar(50) not null,
dataDiNascita char(10) not null,
hashPassword varchar(50) not null,
newUtente boolean not null default true,
ruolo varchar(20) not null
);

create table Supervisore(
email nvarchar(250) primary key not null,
foreign key (email) references Utente(email) on update cascade on delete cascade
);

create table Dipendente(
email nvarchar(250) primary key not null,
supEmail nvarchar(250) not null,
foreign key (email) references Utente(email) on update cascade on delete cascade,
foreign key (supEmail) references Utente(email) on update cascade
);

create table GestoreAccount(
email nvarchar(250) primary key not null,
foreign key (email) references Utente(email) on update cascade on delete cascade
);


create table Task(
id bigint auto_increment primary key not null,
titolo varchar(50) not null,
dataDiScadenza char(10),
dataDiCreazione char(10),
-- Priorita varchar(20) not null,
istruzioni varchar(2000) not null,
stato varchar(20) not null,
supervisore nvarchar(250) not null,
dipendente nvarchar(250) not null,
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
sender nvarchar(250) not null,
receiver nvarchar(250) not null,
foreign key (task_id) references Task(id) on update cascade on delete cascade,
foreign key (sender) references Utente(email) on update cascade on delete cascade,
foreign key (receiver) references Utente(email) on update cascade on delete cascade
);