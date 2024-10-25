package com.franco.pruebabottel;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;


public class MiBotTelegramm extends TelegramLongPollingBot {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/peluqueriapepe";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456789";

    private final Map<Long, String> userInputStage = new HashMap<>();
    private final Map<Long, String> userName = new HashMap<>();
    private final Map<Long, String> userLastName = new HashMap<>();
    private final Map<Long, String> userPhone = new HashMap<>();
    private final Map<Long, Integer> selectedActivityId = new HashMap<>();
    private final Map<Long, String> usernameMap = new HashMap<>();
    private final Map<Long, String> fechaMap = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "Firulais1207Bot"; 
    }

    @Override
    public String getBotToken() {
        return "your_token"; 
    }

@Override
public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.equalsIgnoreCase("hola")) {
            sendWelcomeMessage(chatId);
        } else if (userInputStage.containsKey(chatId)) {
            handleUserInput(update, chatId, messageText);
        }
    } else if (update.hasCallbackQuery()) {
        handleCallbackQuery(update.getCallbackQuery());
    }
}



private void handleCallbackQuery(CallbackQuery callbackQuery) {
    String callbackData = callbackQuery.getData();
    long chatId = callbackQuery.getMessage().getChatId();
    String currentUsername = callbackQuery.getFrom().getUserName();
    long userId = callbackQuery.getFrom().getId();

    if (callbackData.startsWith("actividad_")) {
        int activityId = Integer.parseInt(callbackData.split("_")[1]);
        selectedActivityId.put(chatId, activityId);
        sendMessage(chatId, "Please enter the date in DD/MM format.");
        userInputStage.put(chatId, "prueba");
    } else if (callbackData.startsWith("personaje_")) {
        String personaje = callbackData.split("_")[1];
        System.out.println(personaje + ":00");
        System.out.println(convertDate(fechaMap.get(chatId)));
        
        String h = personaje;
        String[] timeParts = h.split(":");
        
         if (timeParts.length != 2) {
                    throw new IllegalArgumentException("Formato de hora incorrecto");
                }

                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);

                if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                    throw new IllegalArgumentException("Hora o minutos fuera de rango");
                }

                String date = userInputStage.get(chatId);
                String formattedTime = String.format("%02d:%02d:00", hour, minute);
                String formattedDateTime = date + " " + formattedTime;
        
        saveTurnoToDatabase(chatId,convertDate(fechaMap.get(chatId))+" " +personaje + ":00");
        
        userInputStage.remove(chatId);
    } else {
        switch (callbackData) {
            case "agendar":
                usernameMap.put(chatId, callbackQuery.getFrom().getId()+"");
                System.out.println(currentUsername);
                System.out.println("User ID: " + userId);
                if (isUserRegistered(callbackQuery.getFrom().getId()+"")) {
                    showActivities(chatId);
                } else {
                    sendMessage(chatId, "Please enter your name.");
                    userInputStage.put(chatId, "nombre");
                }
                break;
            case "precios":
                sendPrices(chatId);
                break;
            case "horarios":
                sendMessage(chatId, "Our hours are:\n\nMonday to Friday: 9:00 AM - 6:00 PM\n\nSaturdays: 8:00 AM - 11:00 AM");
                break;
            case "elije_personaje":
                sendMessage(chatId, "Please enter the date in DD/MM format.");
                
                userInputStage.put(chatId, "prueba");
                break;
            default:
                sendMessage(chatId, "Opción no reconocida.");
                break;
        }
    }
}


public static String convertDate(String dateString) {
      
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            
            Date date = inputFormat.parse(dateString);

            
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);

         
            calendar.setTime(date);
            calendar.set(Calendar.YEAR, year);

           
            return outputFormat.format(calendar.getTime());

        } catch (ParseException e) {
            
            System.err.println("Error en el formato de fecha: " + e.getMessage());
            return null;
        }
    }


private void searchPersonajes(long chatId, String fecha) {
    
    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    
    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

   
    String query = "SELECT hora FROM turnos WHERE fecha = ?";

    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement stmt = connection.prepareStatement(query)) {
        
       
        Date parsedDate = null;
        try {
            parsedDate = inputFormat.parse(fecha);
        } catch (ParseException ex) {
            Logger.getLogger(MiBotTelegramm.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        if (parsedDate != null) {
            String fechaFormatted = outputFormat.format(parsedDate);
            stmt.setString(1, fechaFormatted);
        } else {
            sendMessage(chatId, "Error al procesar la fecha.");
            return;
        }

      
        ResultSet rs = stmt.executeQuery();

      
        List<String> horasList = new ArrayList<>();

        while (rs.next()) {
          
            String hora = rs.getString("hora");
            horasList.add(hora);
        }

       
        String[] horasArray = horasList.toArray(new String[0]);

        

        showPersonajes(chatId,horasArray);
        
    } catch (SQLException e) {
        e.printStackTrace();
        sendMessage(chatId, "Error al buscar las horas.");
    }
}

private String capitalizeFirstLetter(String str) {
    if (str == null || str.isEmpty()) {
        return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
}


private void showPersonajes(long chatId, String[] horas) {
    InlineKeyboardMarkup personajeMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> personajeRows = new ArrayList<>();

    String fecha = convertDate(fechaMap.get(chatId));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Calendar calendar = Calendar.getInstance();
    
    try {
        calendar.setTime(sdf.parse(fecha));
    } catch (ParseException e) {
        e.printStackTrace();
        sendMessage(chatId, "Error al procesar la fecha.");
        return;
    }
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    
    String[] personajes;
    if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
        personajes = generateHorarios("09:00", "18:00", 30); // Horarios de lunes a viernes
    } else if (dayOfWeek == Calendar.SATURDAY) {
        personajes = generateHorarios("09:00", "12:00", 30); // Horarios los sábados
    } else {
       
        personajes = new String[0];
    }
    
    
    Set<String> horasSet = new HashSet<>(Arrays.asList(horas));

 
    String[] personajesFullFormat = convertToFullFormat(personajes);


    List<InlineKeyboardButton> row = new ArrayList<>();
    for (int i = 0; i < personajesFullFormat.length; i++) {
        String horario = personajesFullFormat[i];
        if (!horasSet.contains(horario)) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(horario.substring(0, 5)); // Mostrar solo HH:mm
            button.setCallbackData("personaje_" + horario.substring(0, 5).toLowerCase());

            row.add(button);

            // Añadir fila completa y crear una nueva fila cada 2 botones
            if (row.size() == 2) {
                personajeRows.add(new ArrayList<>(row));
                row.clear();
            }
        }
    }

    // Añadir la última fila si no está vacía
    if (!row.isEmpty()) {
        personajeRows.add(row);
    }

    personajeMarkup.setKeyboard(personajeRows);
    sendMessage(chatId, "Choose the time slot you prefer:", personajeMarkup);
    userInputStage.put(chatId, "personaje_seleccionado");
}

public static String[] generateHorarios(String startTime, String endTime, int intervalMinutes) {
    List<String> horariosList = new ArrayList<>();
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    try {
        Calendar start = Calendar.getInstance();
        start.setTime(timeFormat.parse(startTime));

        Calendar end = Calendar.getInstance();
        end.setTime(timeFormat.parse(endTime));

        while (start.before(end)) {
            horariosList.add(timeFormat.format(start.getTime()));
            start.add(Calendar.MINUTE, intervalMinutes);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    // Convertir la lista a un arreglo de String
    return horariosList.toArray(new String[0]);
}

private String[] convertToFullFormat(String[] horarios) {
    SimpleDateFormat shortFormat = new SimpleDateFormat("HH:mm");
    SimpleDateFormat fullFormat = new SimpleDateFormat("HH:mm:ss");

    List<String> fullFormatList = new ArrayList<>();
    for (String horario : horarios) {
        try {
            java.util.Date date = shortFormat.parse(horario);
            fullFormatList.add(fullFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    return fullFormatList.toArray(new String[0]);
}



    
   private void handleUserInput(Update update, long chatId, String messageText) {
    String currentStage = userInputStage.get(chatId);

    switch (currentStage) {
        case "nombre":
            userName.put(chatId, messageText);
            userInputStage.put(chatId, "apellido");
            sendMessage(chatId, "Thank you. Now please enter your last name.");
            break;
        case "apellido":
            userLastName.put(chatId, messageText);
            userInputStage.put(chatId, "telefono");
            sendMessage(chatId, "Thank you. Now please enter your phone number.");
            break;
        case "telefono":
            userPhone.put(chatId, messageText);
            saveUserToDatabase(chatId);
            sendMessage(chatId, "Thank you for your registration. ");
            showActivities(chatId);
            userInputStage.remove(chatId);
            userName.remove(chatId);
            userLastName.remove(chatId);
            userPhone.remove(chatId);
            break;
        
        case "personaje_seleccionado":
            if (update.hasCallbackQuery()) {
                String callbackData = update.getCallbackQuery().getData();
                sendMessage(chatId, "Personaje seleccionado: " + callbackData.replace("personaje_", "").replace("_", " "));
                userInputStage.remove(chatId);
            } else {
                sendMessage(chatId, "Error: No se encontró callbackData.");
            }
            break;
        case "prueba":
            fechaMap.put(chatId, messageText);
            System.out.println(gato(fechaMap.get(chatId)));
            searchPersonajes(chatId, gato(fechaMap.get(chatId)));
            
           break;
        default:
            sendMessage(chatId, "Error processing the information.");
            break;
    }
}
   


private void saveTurnoToDatabase(long chatId, String dateTime) {
    String username = usernameMap.get(chatId);
    int activityId = selectedActivityId.get(chatId);

    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement stmt = connection.prepareStatement(
             "INSERT INTO turnos (fecha, hora, estado, idCli, idAct) VALUES (?, ?, 'Pendiente', (SELECT idCli FROM clientes WHERE username = ?), ?)")) {

        String[] dateTimeParts = dateTime.split(" ");
        String date = dateTimeParts[0];
        String time = dateTimeParts[1];

        stmt.setDate(1, java.sql.Date.valueOf(date));
        stmt.setTime(2, java.sql.Time.valueOf(time));
        stmt.setString(3, username);
        stmt.setInt(4, activityId);

        stmt.executeUpdate();

        
        try (PreparedStatement activityStmt = connection.prepareStatement("SELECT descripcion, precio FROM actividades WHERE idAct = ?")) {
            activityStmt.setInt(1, activityId);
            ResultSet rs = activityStmt.executeQuery();
            if (rs.next()) {
                String descripcion = rs.getString("descripcion");
                double precio = rs.getDouble("precio");

                
                String formattedDate = formatDate(date);

                
                sendConfirmationMessage(chatId, formattedDate, time, descripcion, precio);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


private String formatDate(String date) {
    try {
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

       
        java.util.Date parsedDate = inputFormat.parse(date);

       
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        
        calendar.setTime(parsedDate);
        calendar.set(Calendar.YEAR, year);

        
        return outputFormat.format(calendar.getTime());

    } catch (ParseException e) {
        e.printStackTrace();
        return date; 
    }
}


private String gato(String date) {
    try {
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

       
        java.util.Date parsedDate = inputFormat.parse(date);

     
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

      
        calendar.setTime(parsedDate);
        calendar.set(Calendar.YEAR, year);

        
        return outputFormat.format(calendar.getTime());

    } catch (ParseException e) {
        e.printStackTrace();
        return date; 
    }
}



private void sendConfirmationMessage(long chatId, String date, String time, String descripcion, double precio) {
    String messageText = String.format(
        "Tu turno ha sido agendado👍\n\n" +
        "📅 %s\n\n" +
        "🕓 %s\n\n" +
        "💈 %s\n\n" +
        "💵 $%.2f",
        date, time, descripcion, precio);

    sendMessage(chatId, messageText);
}




    private void saveUserToDatabase(long chatId) {
        String username = usernameMap.get(chatId);
        String name = userName.get(chatId);
        String lastName = userLastName.get(chatId);
        String phone = userPhone.get(chatId);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = connection.prepareStatement("INSERT INTO clientes (username, nombre, apellido, telefono) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, username);
            stmt.setString(2, name);
            stmt.setString(3, lastName);
            stmt.setString(4, phone);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showActivities(long chatId) {
        StringBuilder messageText = new StringBuilder("Select an activity:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            String query = "SELECT idAct, descripcion FROM actividades";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int idAct = resultSet.getInt("idAct");
                String descripcion = resultSet.getString("descripcion");
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(descripcion);
                button.setCallbackData("actividad_" + idAct);

                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(button);
                rows.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageText.append("Error retrieving activities.");
        }

        markup.setKeyboard(rows);
        sendMessage(chatId, messageText.toString(), markup);
    }

    private boolean isUserRegistered(String username) {
        boolean isRegistered = false;

        String query = "SELECT COUNT(*) FROM clientes WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                isRegistered = rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isRegistered;
    }

    private void sendPrices(long chatId) {
        StringBuilder messageText = new StringBuilder("Our prices are:\n");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            String query = "SELECT descripcion, precio FROM actividades";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String descripcion = resultSet.getString("descripcion");
                double precio = resultSet.getDouble("precio");
                messageText.append(descripcion).append(": $").append(precio).append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageText.append("Error fetching the prices.");
        }

        sendMessage(chatId, messageText.toString());
    }

    private void sendWelcomeMessage(long chatId) {
    String welcomeText = "Hello, welcome to Pepe's hair salon. How can we assist you today?";
    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    InlineKeyboardButton button1 = new InlineKeyboardButton();
    button1.setText("✂️  Book an Appointment");
    button1.setCallbackData("agendar");

    InlineKeyboardButton button2 = new InlineKeyboardButton();
    button2.setText("✅  Show Prices");
    button2.setCallbackData("precios");

    InlineKeyboardButton button3 = new InlineKeyboardButton();
    button3.setText("⌚  Show Hours");
    button3.setCallbackData("horarios");

    InlineKeyboardButton button4 = new InlineKeyboardButton();
    button4.setText("🌐  Visit our website");
    button4.setUrl("https://www.instagram.com/pelokitos_sn/");

    

    List<InlineKeyboardButton> row1 = new ArrayList<>();
    row1.add(button1);
    row1.add(button2);

    List<InlineKeyboardButton> row2 = new ArrayList<>();
    row2.add(button3);
    row2.add(button4);

  

    rows.add(row1);
    rows.add(row2);
    

    markup.setKeyboard(rows);
    sendMessage(chatId, welcomeText, markup);
}



    private void sendMessage(long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    private void sendMessage(long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        if (markup != null) {
            message.setReplyMarkup(markup);
        }

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
