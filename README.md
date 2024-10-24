# Telegram-appointment-chatbot

# Description
This project is a Java-based Telegram bot designed for a hair salon appointment management system. It allows users to schedule appointments, view pricing, and check the salon's operating hours. The bot interacts with a MySQL database to store user information, activities, and appointment schedules.

# Features
- Welcome Message: Greets users with a welcome message when they send "hola".
- User Registration: Collects user information (name, last name, and phone number) and stores it in a MySQL database.
- Appointment Scheduling: Allows users to select an activity, choose a date, and pick an available time slot for their appointment.
- Dynamic Time Slots: Generates time slots based on the chosen day (weekdays or Saturdays) and shows only available time slots.
- Database Integration: Saves the scheduled appointments in a MySQL database, associating users with their selected activity and time.
