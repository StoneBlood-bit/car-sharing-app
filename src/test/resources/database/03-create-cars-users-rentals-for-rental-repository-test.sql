insert into cars (id, model, brand, type, inventory, daily_fee)
values (1, 'X5', 'BMW', 'SUV', 13, 130.99);

insert into cars (id, model, brand, type, inventory, daily_fee)
values (2, 'RS7', 'Audi', 'SEDAN', 15, 150.99);

insert into cars (id, model, brand, type, inventory, daily_fee)
values (3, 'Laguna4', 'Renault', 'UNIVERSAL', 10, 110.99);

insert into users (id, email, first_name, last_name, password, role, chat_id)
values (1, 'bob@gmail.com', 'Bob', 'Snow', 'password', 'CUSTOMER', '1212');

/*no overdue, completed*/
insert into rentals (id, rental_date, return_date, actual_return_date, car_id, user_id)
values (1, '2025-01-20 14:30', '2025-01-25 14:30', '2025-01-24 14:30', 1, 1);

/*overdue no completed*/
insert into rentals (id, rental_date, return_date, actual_return_date, car_id, user_id)
values (2, '2025-01-20 14:30', '2025-01-25 14:30', null, 2, 1);

/*active*/
insert into rentals (id, rental_date, return_date, actual_return_date, car_id, user_id)
values (3, '2025-01-20 14:30', '2026-01-25 14:30', null, 3, 1);
