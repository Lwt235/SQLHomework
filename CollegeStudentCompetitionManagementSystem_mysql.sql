/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2025/11/19 15:35:25                          */
/*==============================================================*/


drop table if exists Award;

drop table if exists AwardResult;

drop table if exists Competition;

drop table if exists File;

drop table if exists JudgeAssignment;

drop table if exists Registration;

drop table if exists Role;

drop table if exists Rule;

drop table if exists Submission;

drop table if exists SubmissionFile;

drop table if exists Team;

drop table if exists TeamMember;

drop table if exists User;

drop table if exists UserRole;

/*==============================================================*/
/* Table: Award                                                 */
/*==============================================================*/
create table Award
(
   award_id             int not null,
   competition_id       int not null,
   award_name           varchar(200) not null,
   award_level          varchar(50) not null default 'none',
   criteria_description text,
   created_at           datetime not null,
   updated_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (award_id)
);

/*==============================================================*/
/* Table: AwardResult                                           */
/*==============================================================*/
create table AwardResult
(
   registration_id      int not null,
   award_id             int not null,
   award_time           datetime not null,
   certificate_no       varchar(100),
   certificate_path     varchar(500),
   created_at           datetime not null,
   updated_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (registration_id, award_id)
);

/*==============================================================*/
/* Table: Competition                                           */
/*==============================================================*/
create table Competition
(
   competition_id       int not null,
   competition_title    varchar(255) not null,
   short_title          varchar(100),
   competition_status   varchar(20) default 'draft',
   description          text,
   category             varchar(50),
   level                varchar(20) not null default 'school',
   organizer            varchar(200),
   start_date           datetime,
   end_date             datetime,
   signup_start         datetime,
   signup_end           datetime,
   submit_start         datetime,
   submit_end           datetime,
   review_start         datetime,
   review_end           datetime,
   award_publish_date   datetime,
   max_team_size        int,
   created_at           datetime not null,
   updated_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (competition_id)
);

/*==============================================================*/
/* Table: File                                                  */
/*==============================================================*/
create table File
(
   file_id              int not null,
   upload_user_id       int not null,
   storage_key          varchar(500) not null,
   file_name            varchar(255) not null,
   file_type            varchar(32) not null default 'other',
   size_bytes           int,
   checksum             varchar(128),
   version_tag          varchar(50),
   created_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (file_id)
);

/*==============================================================*/
/* Table: JudgeAssignment                                       */
/*==============================================================*/
create table JudgeAssignment
(
   user_id              int not null,
   submission_id        int not null,
   weight               decimal(8,2) not null,
   score                decimal(8,2) not null,
   comment              text,
   created_at           datetime not null,
   updated_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (user_id, submission_id)
);

/*==============================================================*/
/* Table: Registration                                          */
/*==============================================================*/
create table Registration
(
   registration_id      int not null,
   competition_id       int not null,
   team_id              int,
   user_id              int,
   audit_user_id        int,
   registration_status  varchar(20) not null default 'pending',
   audit_time           datetime,
   remark               text,
   created_at           datetime not null,
   updated_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (registration_id)
);

/*==============================================================*/
/* Table: Role                                                  */
/*==============================================================*/
create table Role
(
   role_id              int not null,
   role_code            varchar(50) not null,
   role_name            varchar(100) not null,
   description          text,
   created_at           datetime not null,
   updated_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (role_id)
);

/*==============================================================*/
/* Table: Rule                                                  */
/*==============================================================*/
create table Rule
(
   rule_id              int not null,
   competition_id       int,
   rule_type            varchar(50) not null default 'other',
   content              text not null,
   version_no           int,
   is_active            bool not null default 0,
   created_at           datetime not null,
   updated_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (rule_id)
);

/*==============================================================*/
/* Table: Submission                                            */
/*==============================================================*/
create table Submission
(
   submission_id        int not null,
   registration_id      int not null,
   submission_title     varchar(255) not null,
   abstract             text,
   submission_status    varchar(20) not null default 'draft',
   submitted_at         datetime,
   final_locked_at      datetime,
   created_at           datetime not null,
   updated_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (submission_id)
);

/*==============================================================*/
/* Table: SubmissionFile                                        */
/*==============================================================*/
create table SubmissionFile
(
   file_id              int not null,
   submission_id        int not null,
   primary key (file_id, submission_id)
);

/*==============================================================*/
/* Table: Team                                                  */
/*==============================================================*/
create table Team
(
   team_id              int not null,
   team_name            varchar(200) not null,
   formed_at            datetime,
   description          text,
   created_at           datetime not null,
   updated_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (team_id)
);

/*==============================================================*/
/* Table: TeamMember                                            */
/*==============================================================*/
create table TeamMember
(
   user_id              int not null,
   team_id              int not null,
   role_in_team         varchar(20) not null default 'member',
   primary key (user_id, team_id)
);

/*==============================================================*/
/* Table: User                                                  */
/*==============================================================*/
create table User
(
   user_id              int not null,
   user_status          varchar(20) not null default 'active',
   username             varchar(50) not null,
   password_hash        varchar(256) not null,
   real_name            varchar(100),
   email                varchar(100),
   phone                varchar(20),
   school               varchar(256),
   department           varchar(256),
   student_no           varchar(50),
   auth_type            varchar(20) not null default 'local',
   last_login_at        datetime,
   created_at           datetime not null,
   updated_at           datetime not null,
   is_deleted           bool not null default 0,
   primary key (user_id)
);

/*==============================================================*/
/* Table: UserRole                                              */
/*==============================================================*/
create table UserRole
(
   user_id              int not null,
   role_id              int not null,
   primary key (user_id, role_id)
);

alter table Award add constraint FK_SetUp foreign key (competition_id)
      references Competition (competition_id);

alter table AwardResult add constraint FK_AwardResult foreign key (registration_id)
      references Registration (registration_id);

alter table AwardResult add constraint FK_AwardResult2 foreign key (award_id)
      references Award (award_id);

alter table File add constraint FK_UploadFile foreign key (upload_user_id)
      references User (user_id);

alter table JudgeAssignment add constraint FK_JudgeAssignment foreign key (user_id)
      references User (user_id);

alter table JudgeAssignment add constraint FK_JudgeAssignment2 foreign key (submission_id)
      references Submission (submission_id);

alter table Registration add constraint FK_AuditRegistration foreign key (audit_user_id)
      references User (user_id);

alter table Registration add constraint FK_IndividualRegistration foreign key (user_id)
      references User (user_id);

alter table Registration add constraint FK_ReceiveRegistration foreign key (competition_id)
      references Competition (competition_id);

alter table Registration add constraint FK_TeamRegistration foreign key (team_id)
      references Team (team_id);

alter table Rule add constraint FK_CompetitionRule foreign key (competition_id)
      references Competition (competition_id);

alter table Submission add constraint FK_ReceivedSubmission foreign key (registration_id)
      references Registration (registration_id);

alter table SubmissionFile add constraint FK_SubmissionFile foreign key (file_id)
      references File (file_id);

alter table SubmissionFile add constraint FK_SubmissionFile2 foreign key (submission_id)
      references Submission (submission_id);

alter table TeamMember add constraint FK_TeamMember foreign key (user_id)
      references User (user_id);

alter table TeamMember add constraint FK_TeamMember2 foreign key (team_id)
      references Team (team_id);

alter table UserRole add constraint FK_UserRole foreign key (user_id)
      references User (user_id);

alter table UserRole add constraint FK_UserRole2 foreign key (role_id)
      references Role (role_id);

