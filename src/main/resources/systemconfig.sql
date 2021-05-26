use tch;

create table `systemconfig` (
  `id` bigint(20) not null,
  `name` varchar(255) not null,
  `courts` varchar(255) not null,
  `duration_unit_in_minutes` int(11) not null,
  `max_days_reservation_in_future` int(11) not null,
  `max_duration` int(10) not null,
  `opening_hour` int(10) not null,
  `closing_hour` int(10) not null,
  PRIMARY KEY (`id`)
  );