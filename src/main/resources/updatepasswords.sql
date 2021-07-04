use tch;

update user set password = CONCAT("{bcrypt}",password)
where password not like '{bcrypt}%';