package com.salesiana.gestionbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LaboratorioBot extends TelegramLongPollingBot {

    private Map<Long, UserSession> userSessions = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleTextMessage(Update update) {
    long chatId = update.getMessage().getChatId();
    String messageText = update.getMessage().getText();
    UserSession session = userSessions.computeIfAbsent(chatId, k -> new UserSession());

    if (session.isWaitingForMaterialResponse()) {
        handleMaterialResponse(chatId, session, messageText);
        return;
    }

    if (session.isWaitingForConfirmation()) {
        handleReservationConfirmation(chatId, session, messageText);
        return;
    }

    if (session.isAddingMaterials()) {
        handleAdditionalMaterials(chatId, session, messageText);
        return;
    }

    if (session.isEditingMaterials()) {
        handleEditMaterialsResponse(chatId, session, messageText);
        return;
    }

    if (session.isRemovingMaterials() && session.isWaitingForAnotherRemoval()) {
        handleAnotherRemoval(chatId, session, messageText);
        return;
    }

    if (session.isRemovingMaterials()) {
        handleRemoveMaterialsResponse(chatId, session, messageText);
        return;
    }

    if (session.isUpdatingMaterials()) {
        handleUpdateMaterialsResponse(chatId, session, messageText);
        return;
    }

    if (session.isEditingReservation()) {
        handleEditReservation(chatId, session, messageText);
        return;
    }

    switch (messageText) {
        case "/start":
            sendWelcomeMessage(chatId);
            break;
        case "/login":
            sendLoginInstructions(chatId);
            break;
        case "/reservar":
        case "Reservar laboratorio":
            handleReservationCommand(chatId, session);
            break;
        case "/misreservas":
        case "Mis reservas":
            showUserReservations(chatId, session.getUsername());
            break;
        case "/editar":
        case "Editar reserva":
            showUserReservationsForEdit(chatId, session.getUsername());
            break;
        case "/cancelar":
        case "Cancelar reserva":
            showUserReservationsForCancel(chatId, session.getUsername());
            break;
        case "/logout":
        case "Cerrar sesión":
            handleLogout(chatId, session);
            break;
        case "/menu":
        case "Menú principal":
            showMainMenu(chatId);
            break;
        default:
            handleDefaultCommand(chatId, session, messageText);
            break;
    }
}

    private void handleCallbackQuery(Update update) {
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    String callbackData = update.getCallbackQuery().getData();
    UserSession session = userSessions.get(chatId);
    
    if (session == null || !session.isAuthenticated()) {
        sendMessage(chatId, "🔒 Por favor, inicia sesión primero con /login usuario contraseña");
        return;
    }

    try {
        if (callbackData.startsWith("select_lab_")) {
            handleLabSelection(chatId, callbackData);
        } else if (callbackData.startsWith("select_date_") || callbackData.startsWith("edit_date_")) {
            handleDateSelection(chatId, callbackData, session);
        } else if (callbackData.startsWith("select_time_") || callbackData.startsWith("edit_time_")) {
            handleTimeSelection(chatId, callbackData, session);
        } else if (callbackData.startsWith("select_subject_") || callbackData.startsWith("edit_subject_")) {
            handleSubjectSelection(chatId, callbackData, session);
        } else if (callbackData.startsWith("cancel_reservation_")) {
            handleCancelReservation(chatId, callbackData);
        } else if (callbackData.startsWith("edit_reservation_")) {
            handleEditReservationSelection(chatId, callbackData, session);
        }
    } catch (Exception e) {
        e.printStackTrace();
        sendMessage(chatId, "❌ Error al procesar tu solicitud. Por favor intenta nuevamente.");
    }
}

    private void sendWelcomeMessage(long chatId) {
        sendMessage(chatId, "¡Bienvenido al Sistema de Gestión de Laboratorios!\n\n" +
                "Por favor, inicia sesión con /login usuario contraseña");
    }

    private void sendLoginInstructions(long chatId) {
        sendMessage(chatId, "🔐 Por favor, introduce tu usuario y contraseña en el siguiente formato:\n" +
                "/login usuario contraseña");
    }

    private void handleReservationCommand(long chatId, UserSession session) {
        if (session.isAuthenticated()) {
            showLaboratories(chatId);
        } else {
            sendMessage(chatId, "🔒 Por favor, inicia sesión primero con /login usuario contraseña");
        }
    }

    private void handleDefaultCommand(long chatId, UserSession session, String messageText) {
        if (messageText.startsWith("/login ")) {
            handleLoginCommand(chatId, session, messageText);
        } else {
            sendMessage(chatId, "⚠️ Comando no reconocido. Usa /login para iniciar sesión o /reservar para hacer una reserva.");
        }
    }

    private void handleLoginCommand(long chatId, UserSession session, String messageText) {
        String[] credentials = messageText.split(" ");
        if (credentials.length == 3) {
            String username = credentials[1];
            String password = credentials[2];
            if (authenticate(username, password)) {
                session.setUsername(username);
                session.setAuthenticated(true);
                sendMessage(chatId, "✅ Inicio de sesión exitoso. ¿Qué deseas hacer?");
                showMainMenu(chatId);
            } else {
                sendMessage(chatId, "❌ Credenciales incorrectas. Inténtalo de nuevo.");
            }
        } else {
            sendMessage(chatId, "⚠️ Formato incorrecto. Usa /login usuario contraseña");
        }
    }

    private void showMainMenu(long chatId) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Reservar laboratorio");
        row1.add("Mis reservas");
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Editar reserva");
        row2.add("Cancelar reserva");
        
        KeyboardRow row3 = new KeyboardRow();
        row3.add("Cerrar sesión");
        
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        
        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        
        String menuText = "📋 Menú Principal - Selecciona una opción:\n\n" +
                         "También puedes usar comandos:\n" +
                         "/reservar - Hacer una reserva\n" +
                         "/misreservas - Ver tus reservas\n" +
                         "/editar - Editar una reserva\n" +
                         "/cancelar - Cancelar una reserva\n" +
                         "/logout - Cerrar sesión";
        
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(menuText);
        message.setReplyMarkup(keyboard);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleLogout(long chatId, UserSession session) {
        if (session.isAuthenticated()) {
            userSessions.remove(chatId);
            sendMessage(chatId, "🔒 Sesión cerrada correctamente. Usa /login para iniciar sesión nuevamente.");
        } else {
            sendMessage(chatId, "⚠️ No hay sesión activa para cerrar.");
        }
    }

    private boolean authenticate(String username, String password) {
        String query = "SELECT * FROM Usuarios WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showLaboratories(long chatId) {
        String query = "SELECT nombre FROM Laboratorios WHERE estado = 'Disponible' ORDER BY nombre";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            
            while (rs.next()) {
                String labName = rs.getString("nombre");
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(labName);
                button.setCallbackData("select_lab_" + labName);
                keyboard.add(Collections.singletonList(button));
            }
            
            markup.setKeyboard(keyboard);
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("🔬 Selecciona un laboratorio:");
            message.setReplyMarkup(markup);
            execute(message);
        } catch (SQLException | TelegramApiException e) {
            e.printStackTrace();
            sendMessage(chatId, "❌ Error al cargar los laboratorios. Por favor intenta más tarde.");
        }
    }

    private void handleLabSelection(long chatId, String callbackData) {
        String labName = callbackData.split("_")[2];
        UserSession session = userSessions.get(chatId);
        
        if (session.isEditingReservation()) {
            session.setLabName(labName);
            sendMessage(chatId, "✅ Laboratorio actualizado a: " + labName);
            showEditOptions(chatId);
        } else {
            showCalendar(chatId, labName);
        }
    }

    private void showCalendar(long chatId, String labName) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(date.format(formatter));
            button.setCallbackData("select_date_" + labName + "_" + date);
            keyboard.add(Collections.singletonList(button));
        }
        
        markup.setKeyboard(keyboard);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📅 Selecciona una fecha para el laboratorio " + labName + ":");
        message.setReplyMarkup(markup);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleDateSelection(long chatId, String callbackData, UserSession session) {
    String[] parts = callbackData.split("_");
    String labName = parts[2];
    LocalDate date = LocalDate.parse(parts[3]);
    
    if (callbackData.startsWith("edit_date_")) {
        session.setDate(date);
        showTimeSlotsForEdit(chatId, labName, date);
    } else {
        if (session.isEditingReservation()) {
            session.setDate(date);
            showTimeSlotsForEdit(chatId, labName, date);
        } else {
            showTimeSlots(chatId, labName, date);
        }
    }
}

    private void showTimeSlotsForEdit(long chatId, String labName, LocalDate date) {
    System.out.println("showTimeSlotsForEdit called for lab: " + labName + ", date: " + date);
    String[][] timeSlots = {
            {"07:30", "09:00"}, {"09:15", "10:45"}, {"11:00", "12:30"},
            {"13:30", "15:00"}, {"15:15", "16:45"}, {"17:00", "18:15"},
            {"18:30", "20:00"}, {"20:15", "21:45"}, {"21:45", "22:30"}
    };
    
    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    
    for (String[] timePair : timeSlots) {
        LocalTime startTime = LocalTime.parse(timePair[0]);
        LocalTime endTime = LocalTime.parse(timePair[1]);
        
        if (isTimeSlotAvailableForEdit(labName, date, startTime, endTime, 
            userSessions.get(chatId).getEditingReservationId())) {
            
            String timeSlotText = startTime + " - " + endTime;
            String callbackData = "edit_time_" + labName + "_" + date + "_" + startTime + "_" + endTime;
            
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(timeSlotText);
            button.setCallbackData(callbackData);
            keyboard.add(Collections.singletonList(button));
        }
    }
    
    markup.setKeyboard(keyboard);
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText("⏰ Selecciona un nuevo horario para el " + 
                  date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ":");
    message.setReplyMarkup(markup);
    
    try {
        execute(message);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }
}

    private boolean isTimeSlotAvailableForEdit(String labName, LocalDate date, LocalTime startTime, LocalTime endTime, int currentReservationId) {
    String query = "SELECT * FROM Reservas r " +
            "JOIN Laboratorios l ON r.Nro_Laboratorio = l.Id_Laboratorio " +
            "WHERE l.nombre = ? AND r.fecha_reserva = ? AND r.Id_Reserva != ? " +
            "AND ((r.hora_inicio BETWEEN ? AND ?) OR " +
            "(r.hora_fin BETWEEN ? AND ?) OR " +
            "(r.hora_inicio <= ? AND r.hora_fin >= ?))";
    
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, labName);
        stmt.setDate(2, Date.valueOf(date));
        stmt.setInt(3, currentReservationId);
        stmt.setTime(4, Time.valueOf(startTime));
        stmt.setTime(5, Time.valueOf(endTime));
        stmt.setTime(6, Time.valueOf(startTime));
        stmt.setTime(7, Time.valueOf(endTime));
        stmt.setTime(8, Time.valueOf(startTime));
        stmt.setTime(9, Time.valueOf(endTime));
        
        ResultSet rs = stmt.executeQuery();
        return !rs.next();
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    
    private void showTimeSlots(long chatId, String labName, LocalDate date) {
        String[][] timeSlots = {
                {"07:30 - 09:00", "09:15 - 10:45", "11:00 - 12:30"},
                {"13:30 - 15:00", "15:15 - 16:45", "17:00 - 18:15"},
                {"18:30 - 20:00", "20:15 - 21:45", "21:45 - 22:30"}
        };
        
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        
        for (String[] timeBlock : timeSlots) {
            for (String timeSlot : timeBlock) {
                String[] times = timeSlot.split(" - ");
                LocalTime startTime = LocalTime.parse(times[0]);
                LocalTime endTime = LocalTime.parse(times[1]);
                
                if (isTimeSlotAvailable(labName, date, startTime, endTime)) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(timeSlot);
                    button.setCallbackData("select_time_" + labName + "_" + date + "_" + startTime + "_" + endTime);
                    keyboard.add(Collections.singletonList(button));
                }
            }
        }
        
        markup.setKeyboard(keyboard);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("⏰ Selecciona un horario para el " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ":");
        message.setReplyMarkup(markup);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isTimeSlotAvailable(String labName, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String query = "SELECT * FROM Reservas r " +
                "JOIN Laboratorios l ON r.Nro_Laboratorio = l.Id_Laboratorio " +
                "WHERE l.nombre = ? AND r.fecha_reserva = ? " +
                "AND ((r.hora_inicio BETWEEN ? AND ?) OR " +
                "(r.hora_fin BETWEEN ? AND ?) OR " +
                "(r.hora_inicio <= ? AND r.hora_fin >= ?))";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, labName);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, Time.valueOf(startTime));
            stmt.setTime(4, Time.valueOf(endTime));
            stmt.setTime(5, Time.valueOf(startTime));
            stmt.setTime(6, Time.valueOf(endTime));
            stmt.setTime(7, Time.valueOf(startTime));
            stmt.setTime(8, Time.valueOf(endTime));
            
            ResultSet rs = stmt.executeQuery();
            return !rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleTimeSelection(long chatId, String callbackData, UserSession session) {
    System.out.println("handleTimeSelection called with callbackData: " + callbackData);
    String[] parts = callbackData.split("_");
    
    if (parts.length < 6) {
        System.out.println("Invalid callbackData format: " + callbackData);
        sendMessage(chatId, "❌ Formato de datos incorrecto. Por favor intenta nuevamente.");
        return;
    }
    
    String labName = parts[2];
    LocalDate date = LocalDate.parse(parts[3]);
    LocalTime startTime = LocalTime.parse(parts[4]);
    LocalTime endTime = LocalTime.parse(parts[5]);

    // Establecer los valores en la sesión
    session.setLabName(labName);
    session.setDate(date);
    session.setStartTime(startTime);
    session.setEndTime(endTime);
    
    if (callbackData.startsWith("edit_time_") || session.isEditingReservation()) {
        System.out.println("Updating time for edit");
        sendMessage(chatId, "✅ Horario actualizado a: " + startTime + " - " + endTime);
        showEditOptions(chatId);
    } else {
        System.out.println("Setting time for new reservation");
        // Limpiar el estado de edición para asegurarnos que es una nueva reserva
        session.setEditingReservation(false);
        session.setEditingReservationId(-1);
        askForSubject(chatId);
    }
}
    
    private void askForSubject(long chatId) {
    UserSession session = userSessions.get(chatId);
    
    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    
    String[] subjects = {"Electrónica", "Hardware", "Redes y Telecomunicaciones"};
    
    for (String subject : subjects) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(subject);
        // Añadir prefijo según el modo
        String prefix = session.isEditingReservation() ? "edit_subject_" : "select_subject_";
        button.setCallbackData(prefix + subject.replace(" ", "_"));
        keyboard.add(Collections.singletonList(button));
    }
    
    markup.setKeyboard(keyboard);
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText("📚 Selecciona la materia:");
    message.setReplyMarkup(markup);
    
    try {
        execute(message);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }
}

    private void handleSubjectSelection(long chatId, String callbackData, UserSession session) {
    String subject = callbackData.replace("select_subject_", "").replace("edit_subject_", "").replace("_", " ");
    session.setSubject(subject);
    
    if (callbackData.startsWith("edit_subject_")) {
        sendMessage(chatId, "✅ Materia actualizada a: " + subject);
        showEditOptions(chatId);
    } else {
        askForAdditionalMaterials(chatId);
    }
}

    private void askForAdditionalMaterials(long chatId) {
    UserSession session = userSessions.get(chatId);
    int labId = getLabIdByName(session.getLabName());
    
    if (labId == -1) {
        sendMessage(chatId, "❌ Error: Laboratorio no encontrado");
        return;
    }
    
    List<MaterialAdicional> materials = getAvailableMaterials(labId);
    session.setAvailableMaterials(materials);
    
    if (materials.isEmpty()) {
        // Preguntar si quiere añadir materiales aunque no haya disponibles
        sendMessage(chatId, "ℹ️ Actualmente no hay materiales adicionales disponibles en este laboratorio.\n" +
                          "¿Deseas que te notifiquemos cuando haya disponibilidad? (Responde 'Sí' o 'No')");
        session.setWaitingForMaterialResponse(true);
        session.setNoMaterialsAvailable(true);
    } else {
        // Mostrar materiales disponibles
        StringBuilder materialsList = new StringBuilder();
        materialsList.append("📦 Materiales disponibles:\n\n");
        
        for (int i = 0; i < materials.size(); i++) {
            MaterialAdicional material = materials.get(i);
            materialsList.append(i+1).append(". ").append(material.getName())
                      .append(" (Disponibles: ").append(material.getQuantity()).append(")\n");
        }
        
        materialsList.append("\n¿Deseas reservar algún material adicional? (Responde 'Sí' o 'No')");
        
        sendMessage(chatId, materialsList.toString());
        session.setWaitingForMaterialResponse(true);
        session.setNoMaterialsAvailable(false);
    }
}

    private void handleMaterialResponse(long chatId, UserSession session, String response) {
    response = response.trim().toLowerCase();
    
    if (response.equals("sí") || response.equals("si")) {
        session.setWaitingForMaterialResponse(false);
        
        if (session.isNoMaterialsAvailable()) {
            // Guardar interés en materiales
            sendMessage(chatId, "✅ Hemos registrado tu interés. Te notificaremos cuando haya materiales disponibles.");
            confirmReservation(chatId);
        } else {
            session.setAddingMaterials(true);
            askForMaterialDetails(chatId);
        }
    } else if (response.equals("no")) {
        session.setWaitingForMaterialResponse(false);
        confirmReservation(chatId);
    } else {
        sendMessage(chatId, "⚠️ Por favor, responde 'Sí' o 'No'");
    }
}

    private void askForMaterialDetails(long chatId) {
    UserSession session = userSessions.get(chatId);
    List<MaterialAdicional> materials = session.getAvailableMaterials();
    
    StringBuilder sb = new StringBuilder();
    sb.append("📦 Materiales disponibles:\n\n");
    
    for (int i = 0; i < materials.size(); i++) {
        MaterialAdicional material = materials.get(i);
        sb.append(i+1).append(". ").append(material.getName())
          .append(" (Disponibles: ").append(material.getQuantity()).append(")\n");
    }
    
    sb.append("\n✏️ Para reservar materiales, escribe el número y la cantidad separados por espacio");
    sb.append("\nEjemplo: \"1 2\" para reservar 2 unidades del material 1");
    sb.append("\n\n🏁 Escribe \"fin\" cuando termines");
    
    sendMessage(chatId, sb.toString());
    session.setAddingMaterials(true); // Asegurarse de que el estado esté establecido
}


    private void handleAdditionalMaterials(long chatId, UserSession session, String input) {
    input = input.trim();
    
    if (input.equalsIgnoreCase("fin")) {
        session.setAddingMaterials(false);
        confirmReservation(chatId);
        return;
    }
    
    try {
        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            sendMessage(chatId, "⚠️ Formato incorrecto. Usa: número cantidad (ejemplo: 1 2)");
            return;
        }
        
        int materialIndex = Integer.parseInt(parts[0]) - 1;
        int quantity = Integer.parseInt(parts[1]);
        
        if (materialIndex < 0 || materialIndex >= session.getAvailableMaterials().size()) {
            sendMessage(chatId, "❌ Número de material inválido. Ingresa un número entre 1 y " + 
                      session.getAvailableMaterials().size());
            return;
        }
        
        MaterialAdicional selectedMaterial = session.getAvailableMaterials().get(materialIndex);
        
        if (quantity <= 0) {
            sendMessage(chatId, "❌ La cantidad debe ser mayor a cero");
            return;
        }
        
        if (quantity > selectedMaterial.getQuantity()) {
            sendMessage(chatId, "❌ Cantidad insuficiente. Solo hay " + selectedMaterial.getQuantity() + 
                      " disponibles de " + selectedMaterial.getName());
            return;
        }
        
        session.addSelectedMaterial(selectedMaterial.getName(), quantity);
        sendMessage(chatId, "✅ Añadido: " + selectedMaterial.getName() + " x" + quantity + 
                  "\nPuedes añadir otro material o escribir 'fin' para terminar");
        
    } catch (NumberFormatException e) {
        sendMessage(chatId, "⚠️ Por favor, ingresa solo números en el formato: número cantidad (ejemplo: 1 2)");
    }
}

    private List<MaterialAdicional> getAvailableMaterials(int labId) {
        String query = "SELECT nombre_objeto, cantidad FROM Material_Adicional WHERE Id_Laboratorio = ?";
        List<MaterialAdicional> materials = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, labId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                materials.add(new MaterialAdicional(
                    rs.getString("nombre_objeto"), 
                    rs.getInt("cantidad")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return materials;
    }

    private void confirmReservation(long chatId) {
        UserSession session = userSessions.get(chatId);
        
        StringBuilder sb = new StringBuilder();
        sb.append("📝 Resumen de reserva:\n\n");
        sb.append("🔬 Laboratorio: ").append(session.getLabName()).append("\n");
        sb.append("📅 Fecha: ").append(session.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        sb.append("⏰ Horario: ").append(session.getStartTime()).append(" - ").append(session.getEndTime()).append("\n");
        sb.append("📚 Materia: ").append(session.getSubject()).append("\n");
        
        if (!session.getSelectedMaterials().isEmpty()) {
            sb.append("\n📦 Materiales adicionales:\n");
            session.getSelectedMaterials().forEach((name, qty) -> 
                sb.append("- ").append(name).append(": ").append(qty).append("\n"));
        }
        
        sb.append("\n¿Confirmar reserva? (Responde 'Sí' o 'No')");
        
        session.setWaitingForConfirmation(true);
        sendMessage(chatId, sb.toString());
    }

    private void handleReservationConfirmation(long chatId, UserSession session, String response) {
    response = response.trim().toLowerCase();
    
    if (response.equals("sí") || response.equals("si")) {
        if (insertReservation(chatId)) {
            // Restablecer el estado de la sesión
            session.setWaitingForConfirmation(false);
            session.setWaitingForMaterialResponse(false);
            session.setAddingMaterials(false);
            session.setEditingMaterials(false);
            session.setRemovingMaterials(false);
            session.setUpdatingMaterials(false);
            session.setEditingReservation(false);
            session.setEditingReservationId(-1);
            session.clearSelectedMaterials();

            // Mensaje de éxito
            SendMessage successMessage = new SendMessage();
            successMessage.setChatId(String.valueOf(chatId));
            successMessage.setText("✅ Reserva #" + session.getEditingReservationId() + " creada exitosamente\n\n" +
                                 "📋 Menú Principal - Selecciona una opción:");

            // Teclado principal
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();
            
            KeyboardRow row1 = new KeyboardRow();
            row1.add("Reservar laboratorio");
            row1.add("Mis reservas");
            
            KeyboardRow row2 = new KeyboardRow();
            row2.add("Editar reserva");
            row2.add("Cancelar reserva");
            
            KeyboardRow row3 = new KeyboardRow();
            row3.add("Cerrar sesión");
            
            keyboard.add(row1);
            keyboard.add(row2);
            keyboard.add(row3);
            
            keyboardMarkup.setKeyboard(keyboard);
            keyboardMarkup.setResizeKeyboard(true);
            successMessage.setReplyMarkup(keyboardMarkup);
            
            try {
                execute(successMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    } else if (response.equals("no")) {
        session.setWaitingForConfirmation(false);
        session.clearSelectedMaterials();
        sendMessage(chatId, "❌ Reserva cancelada. Puedes iniciar una nueva con /reservar");
    } else {
        sendMessage(chatId, "⚠️ Por favor, responde 'Sí' o 'No'");
    }
}

    private boolean insertReservation(long chatId) {
        UserSession session = userSessions.get(chatId);
        
        // Validar horarios
        if (session.getEndTime().isBefore(session.getStartTime())) {
            sendMessage(chatId, "❌ Error: La hora de fin debe ser posterior a la hora de inicio");
            return false;
        }
        
        int labId = getLabIdByName(session.getLabName());
        if (labId == -1) {
            sendMessage(chatId, "❌ Error: Laboratorio no encontrado");
            return false;
        }
        
        int userId = getUserIdByUsername(session.getUsername());
        if (userId == -1) {
            sendMessage(chatId, "❌ Error: Usuario no encontrado");
            return false;
        }
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Insertar reserva principal
            String insertQuery = "INSERT INTO Reservas (Nro_Laboratorio, tipo_de_prestamo, materia, " +
                               "fecha_reserva, hora_inicio, hora_fin, estado, id_usuario, usa_material_adicional) " +
                               "VALUES (?, 'Clase', ?, ?, ?, ?, 'Pendiente', ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, labId);
                stmt.setString(2, session.getSubject());
                stmt.setDate(3, Date.valueOf(session.getDate()));
                stmt.setTime(4, Time.valueOf(session.getStartTime()));
                stmt.setTime(5, Time.valueOf(session.getEndTime()));
                stmt.setInt(6, userId);
                stmt.setBoolean(7, !session.getSelectedMaterials().isEmpty());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    sendMessage(chatId, "❌ Error al crear la reserva");
                    return false;
                }
                
                // Obtener ID de la reserva creada
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int reservationId = generatedKeys.getInt(1);
                        
                        // Insertar materiales adicionales si los hay
                        if (!session.getSelectedMaterials().isEmpty()) {
                            insertReservationMaterials(conn, reservationId, labId, session.getSelectedMaterials());
                        }
                        
                        sendMessage(chatId, "✅ Reserva #" + reservationId + " creada exitosamente");
                        session.clearSelectedMaterials();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendMessage(chatId, "❌ Error al crear la reserva: " + e.getMessage());
        }
        
        return false;
    }

    private void insertReservationMaterials(Connection conn, int reservationId, int labId, 
                                      Map<String, Integer> materials) throws SQLException {
        String insertQuery = "INSERT INTO Reserva_Material_Adicional (Id_Reserva, N_Objeto, cantidad) " +
                           "VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            for (Map.Entry<String, Integer> entry : materials.entrySet()) {
                int materialId = getMaterialIdByName(entry.getKey(), labId);
                if (materialId != -1) {
                    stmt.setInt(1, reservationId);
                    stmt.setInt(2, materialId);
                    stmt.setInt(3, entry.getValue());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }

    private void showUserReservations(long chatId, String username) {
    String query = "SELECT r.Id_Reserva, l.nombre, r.fecha_reserva, r.hora_inicio, r.hora_fin, " +
                 "r.materia, r.estado, r.usa_material_adicional " +
                 "FROM Reservas r " +
                 "JOIN Laboratorios l ON r.Nro_Laboratorio = l.Id_Laboratorio " +
                 "JOIN Usuarios u ON r.id_usuario = u.id_usuario " +
                 "WHERE u.username = ? AND r.fecha_reserva >= CURDATE() " +
                 "ORDER BY r.fecha_reserva, r.hora_inicio";
    
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        
        StringBuilder sb = new StringBuilder();
        sb.append("📋 **Tus reservas activas:**\n\n");
        
        boolean hasReservations = false;
        while (rs.next()) {
            hasReservations = true;
            sb.append("🆔 **ID:** ").append(rs.getInt("Id_Reserva")).append("\n");
            sb.append("🔬 **Laboratorio:** ").append(rs.getString("nombre")).append("\n");
            sb.append("📅 **Fecha:** ").append(rs.getDate("fecha_reserva")).append("\n");
            sb.append("⏰ **Horario:** ").append(rs.getTime("hora_inicio")).append(" - ")
             .append(rs.getTime("hora_fin")).append("\n");
            sb.append("📚 **Materia:** ").append(rs.getString("materia")).append("\n");
            sb.append("🔄 **Estado:** ").append(rs.getString("estado")).append("\n");
            
            if (rs.getBoolean("usa_material_adicional")) {
                sb.append("📦 **Materiales adicionales:** Sí\n");
            }
            
            sb.append("\n");
        }
        
        if (!hasReservations) {
            sb.append("No tienes reservas activas actualmente.\n");
        }
        
        sb.append("Usa /reservar para crear una nueva reserva o /editar para modificar una existente.");
        
        // Teclado para acciones después de ver reservas
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Reservar laboratorio");
        row1.add("Editar reserva");
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Cancelar reserva");
        row2.add("Menú principal");
        
        keyboard.add(row1);
        keyboard.add(row2);
        
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(sb.toString());
        message.setReplyMarkup(keyboardMarkup);
        message.setParseMode("Markdown");
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    } catch (SQLException e) {
        e.printStackTrace();
        sendMessage(chatId, "❌ Error al cargar tus reservas. Por favor intenta más tarde.");
    }
}

    private void showUserReservationsForEdit(long chatId, String username) {
        String query = "SELECT r.Id_Reserva, l.nombre, r.fecha_reserva, r.hora_inicio, r.hora_fin " +
                      "FROM Reservas r " +
                      "JOIN Laboratorios l ON r.Nro_Laboratorio = l.Id_Laboratorio " +
                      "JOIN Usuarios u ON r.id_usuario = u.id_usuario " +
                      "WHERE u.username = ? AND r.fecha_reserva >= CURDATE() AND r.estado = 'Pendiente' " +
                      "ORDER BY r.fecha_reserva, r.hora_inicio";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            
            boolean hasReservations = false;
            while (rs.next()) {
                hasReservations = true;
                int reservationId = rs.getInt("Id_Reserva");
                String labName = rs.getString("nombre");
                Date date = rs.getDate("fecha_reserva");
                Time startTime = rs.getTime("hora_inicio");
                Time endTime = rs.getTime("hora_fin");
                
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(labName + " - " + date + " " + startTime + "-" + endTime);
                button.setCallbackData("edit_reservation_" + reservationId);
                
                keyboard.add(Collections.singletonList(button));
            }
            
            if (hasReservations) {
                markup.setKeyboard(keyboard);
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("✏️ Selecciona la reserva que deseas editar:");
                message.setReplyMarkup(markup);
                execute(message);
            } else {
                sendMessage(chatId, "No tienes reservas pendientes que puedas editar.");
            }
        } catch (SQLException | TelegramApiException e) {
            e.printStackTrace();
            sendMessage(chatId, "❌ Error al cargar tus reservas. Por favor intenta más tarde.");
        }
    }

    private void handleEditReservationSelection(long chatId, String callbackData, UserSession session) {
    int reservationId = Integer.parseInt(callbackData.split("_")[2]);
    session.setEditingReservationId(reservationId);
    session.setEditingReservation(true);
    
    // Cargar datos de la reserva existente
    String query = "SELECT r.*, l.nombre as lab_name FROM Reservas r " +
                 "JOIN Laboratorios l ON r.Nro_Laboratorio = l.Id_Laboratorio " +
                 "WHERE r.Id_Reserva = ?";
    
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setInt(1, reservationId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            session.setLabName(rs.getString("lab_name"));
            session.setDate(rs.getDate("fecha_reserva").toLocalDate());
            session.setStartTime(rs.getTime("hora_inicio").toLocalTime());
            session.setEndTime(rs.getTime("hora_fin").toLocalTime());
            session.setSubject(rs.getString("materia"));
            
            // Cargar materiales existentes
            loadExistingMaterials(chatId, reservationId);
            
            // Mostrar resumen de la reserva a editar
            StringBuilder sb = new StringBuilder();
            sb.append("✏️ **Editando reserva #").append(reservationId).append("**\n\n");
            sb.append("🔬 Laboratorio: ").append(session.getLabName()).append("\n");
            sb.append("📅 Fecha: ").append(session.getDate()).append("\n");
            sb.append("⏰ Horario: ").append(session.getStartTime()).append(" - ").append(session.getEndTime()).append("\n");
            sb.append("📚 Materia: ").append(session.getSubject()).append("\n");
            
            if (!session.getSelectedMaterials().isEmpty()) {
                sb.append("\n📦 Materiales adicionales:\n");
                session.getSelectedMaterials().forEach((name, qty) -> 
                    sb.append("- ").append(name).append(": ").append(qty).append("\n"));
            }
            
            // Mostrar opciones de edición
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();
            
            KeyboardRow row1 = new KeyboardRow();
            row1.add("Cambiar laboratorio");
            row1.add("Cambiar fecha/hora");
            
            KeyboardRow row2 = new KeyboardRow();
            row2.add("Modificar materiales");
            row2.add("Cambiar materia");
            
            KeyboardRow row3 = new KeyboardRow();
            row3.add("Confirmar cambios");
            row3.add("Cancelar edición");
            
            keyboard.add(row1);
            keyboard.add(row2);
            keyboard.add(row3);
            
            keyboardMarkup.setKeyboard(keyboard);
            keyboardMarkup.setResizeKeyboard(true);
            
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(sb.toString());
            message.setReplyMarkup(keyboardMarkup);
            
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            sendMessage(chatId, "❌ Reserva no encontrada");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        sendMessage(chatId, "❌ Error al cargar la reserva. Por favor intenta más tarde.");
    }
}

    private void loadExistingMaterials(long chatId, int reservationId) {
        String query = "SELECT ma.nombre_objeto, rma.cantidad " +
                      "FROM Reserva_Material_Adicional rma " +
                      "JOIN Material_Adicional ma ON rma.N_Objeto = ma.N_Objeto " +
                      "WHERE rma.Id_Reserva = ?";
        
        UserSession session = userSessions.get(chatId);
        session.clearSelectedMaterials();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                session.addSelectedMaterial(
                    rs.getString("nombre_objeto"), 
                    rs.getInt("cantidad")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEditOptions(long chatId) {
    System.out.println("showEditOptions called");
    UserSession session = userSessions.get(chatId);
    
    String messageText = "✏️ Opciones de edición\n\n" +
                       "Puedes usar comandos:\n" +
                       "/cambiar_lab - Cambiar laboratorio\n" +
                       "/cambiar_fecha - Cambiar fecha y hora\n" +
                       "/cambiar_materia - Cambiar materia\n" +
                       "/materiales - Gestionar materiales\n" +
                       "/confirmar - Confirmar cambios\n" +
                       "/cancelar - Cancelar edición\n\n" +
                       "O selecciona una opción:";

    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    List<KeyboardRow> keyboard = new ArrayList<>();
    
    KeyboardRow row1 = new KeyboardRow();
    row1.add("Cambiar laboratorio");
    row1.add("Cambiar fecha/hora");
    
    KeyboardRow row2 = new KeyboardRow();
    row2.add("Cambiar materia");
    row2.add("Modificar materiales");
    
    KeyboardRow row3 = new KeyboardRow();
    row3.add("Confirmar cambios");
    row3.add("Cancelar edición");
    
    keyboard.add(row1);
    keyboard.add(row2);
    keyboard.add(row3);
    
    keyboardMarkup.setKeyboard(keyboard);
    keyboardMarkup.setResizeKeyboard(true);
    
    SendMessage message = new SendMessage();
    message.setChatId(String.valueOf(chatId));
    message.setText(messageText);
    message.setReplyMarkup(keyboardMarkup);
    
    try {
        execute(message);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }
}

    private void handleEditReservation(long chatId, UserSession session, String messageText) {
    switch (messageText.toLowerCase()) {
        case "cambiar laboratorio":
        case "/cambiar_lab":
            showLaboratories(chatId);
            break;
            
        case "cambiar fecha/hora":
        case "/cambiar_fecha":
            showCalendarForEdit(chatId, session.getLabName());
            break;
            
        case "cambiar materia":
        case "/cambiar_materia":
            // Forzar el modo de edición antes de pedir la materia
            session.setEditingReservation(true);
            askForSubject(chatId);
            break;
            
        case "modificar materiales":
        case "/materiales":
            handleEditMaterials(chatId);
            break;
            
        case "confirmar cambios":
        case "/confirmar":
            updateReservation(chatId);
            break;
            
        case "cancelar edición":
        case "/cancelar":
            session.setEditingReservation(false);
            session.setEditingReservationId(-1);
            session.clearSelectedMaterials();
            sendMessage(chatId, "❌ Edición cancelada. No se realizaron cambios.");
            showMainMenu(chatId);
            break;
            
        default:
            sendMessage(chatId, "⚠️ Opción no reconocida. Por favor usa los comandos o el teclado proporcionado.");
            break;
    }
}

    private void showCalendarForEdit(long chatId, String labName) {
    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    
    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
    
    for (int i = 0; i < 7; i++) {
        LocalDate date = today.plusDays(i);
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(date.format(formatter));
        button.setCallbackData("edit_date_" + labName + "_" + date);
        keyboard.add(Collections.singletonList(button));
    }
    
    markup.setKeyboard(keyboard);
    
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText("📅 Selecciona una nueva fecha para el laboratorio " + labName + ":");
    message.setReplyMarkup(markup);
    
    try {
        execute(message);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }
}
    
    private void handleEditMaterials(long chatId) {
    UserSession session = userSessions.get(chatId);
    session.setEditingMaterials(true);
    
    int labId = getLabIdByName(session.getLabName());
    List<MaterialAdicional> materials = getAvailableMaterials(labId);
    session.setAvailableMaterials(materials);
    
    StringBuilder sb = new StringBuilder();
    sb.append("📦 Gestión de materiales:\n\n");
    
    // Mostrar materiales actuales
    if (!session.getSelectedMaterials().isEmpty()) {
        sb.append("Materiales actuales en tu reserva:\n");
        session.getSelectedMaterials().forEach((name, qty) -> 
            sb.append("- ").append(name).append(": ").append(qty).append(" unidades\n"));
        sb.append("\n");
    }
    
    // Mostrar materiales disponibles
    if (!materials.isEmpty()) {
        sb.append("Materiales disponibles:\n");
        for (int i = 0; i < materials.size(); i++) {
            MaterialAdicional material = materials.get(i);
            sb.append(i+1).append(". ").append(material.getName())
              .append(" (Disponibles: ").append(material.getQuantity()).append(")\n");
        }
    } else {
        sb.append("ℹ️ No hay materiales adicionales disponibles actualmente.\n");
    }
    
    sb.append("\nOpciones:\n");
    sb.append("1. Añadir nuevos materiales\n");
    sb.append("2. Eliminar materiales existentes\n");
    sb.append("3. Modificar cantidades\n");
    sb.append("4. Finalizar edición de materiales");
    
    sendMessage(chatId, sb.toString());
}


    private void handleEditMaterialsResponse(long chatId, UserSession session, String messageText) {
    messageText = messageText.trim();
    
    if (messageText.equals("1")) {
        askForMaterialDetails(chatId);
    } else if (messageText.equals("2")) {
        showMaterialsToRemove(chatId);
    } else if (messageText.equals("3")) {
        showMaterialsToUpdate(chatId);
    } else if (messageText.equals("4")) {
        session.setEditingMaterials(false);
        showEditOptions(chatId);
    } else {
        sendMessage(chatId, "⚠️ Por favor, selecciona una opción válida (1-4)");
    }
}

    private void handleRemoveMaterialsResponse(long chatId, UserSession session, String messageText) {
    messageText = messageText.trim();
    
    if (messageText.equalsIgnoreCase("cancelar")) {
        session.setRemovingMaterials(false);
        handleEditMaterials(chatId);
        return;
    }
    
    try {
        int materialIndex = Integer.parseInt(messageText) - 1;
        List<String> materialNames = new ArrayList<>(session.getSelectedMaterials().keySet());
        
        if (materialIndex >= 0 && materialIndex < materialNames.size()) {
            String materialName = materialNames.get(materialIndex);
            session.getSelectedMaterials().remove(materialName);
            sendMessage(chatId, "✅ Material eliminado: " + materialName);
            
            // Preguntar si desea eliminar otro material
            sendMessage(chatId, "¿Deseas eliminar otro material? (Sí/No)");
            session.setWaitingForAnotherRemoval(true);
        } else {
            sendMessage(chatId, "❌ Número de material inválido");
        }
    } catch (NumberFormatException e) {
        sendMessage(chatId, "⚠️ Por favor, ingresa solo números");
    }
}
    
    private void handleAnotherRemoval(long chatId, UserSession session, String response) {
    response = response.trim().toLowerCase();
    
    if (response.equals("sí") || response.equals("si")) {
        showMaterialsToRemove(chatId);
    } else if (response.equals("no")) {
        session.setRemovingMaterials(false);
        session.setWaitingForAnotherRemoval(false);
        handleEditMaterials(chatId);
    } else {
        sendMessage(chatId, "⚠️ Por favor, responde 'Sí' o 'No'");
    }
}

    private void handleUpdateMaterialsResponse(long chatId, UserSession session, String messageText) {
        messageText = messageText.trim();
        
        if (messageText.equalsIgnoreCase("cancelar")) {
            session.setUpdatingMaterials(false);
            handleEditMaterials(chatId);
            return;
        }
        
        try {
            int materialIndex = Integer.parseInt(messageText) - 1;
            List<String> materialNames = new ArrayList<>(session.getSelectedMaterials().keySet());
            
            if (materialIndex >= 0 && materialIndex < materialNames.size()) {
                String materialName = materialNames.get(materialIndex);
                session.setCurrentMaterialToUpdate(materialName);
                sendMessage(chatId, "Ingresa la nueva cantidad para " + materialName + ":");
                session.setUpdatingQuantity(true);
            } else {
                sendMessage(chatId, "❌ Número de material inválido");
                session.setUpdatingMaterials(false);
                handleEditMaterials(chatId);
            }
        } catch (NumberFormatException e) {
            sendMessage(chatId, "⚠️ Por favor, ingresa solo números");
        }
    }

    private void showMaterialsToRemove(long chatId) {
        UserSession session = userSessions.get(chatId);
        
        if (session.getSelectedMaterials().isEmpty()) {
            sendMessage(chatId, "ℹ️ No hay materiales para eliminar.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("❌ Selecciona el material a eliminar:\n\n");
        
        List<String> materialNames = new ArrayList<>(session.getSelectedMaterials().keySet());
        for (int i = 0; i < materialNames.size(); i++) {
            sb.append(i+1).append(". ").append(materialNames.get(i)).append("\n");
        }
        
        sb.append("\nEscribe el número del material que deseas eliminar");
        sb.append("\nEscribe \"cancelar\" para volver atrás");
        
        sendMessage(chatId, sb.toString());
        session.setRemovingMaterials(true);
    }

    private void showMaterialsToUpdate(long chatId) {
        UserSession session = userSessions.get(chatId);
        
        if (session.getSelectedMaterials().isEmpty()) {
            sendMessage(chatId, "ℹ️ No hay materiales para modificar.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("✏️ Selecciona el material a modificar:\n\n");
        
        List<String> materialNames = new ArrayList<>(session.getSelectedMaterials().keySet());
        for (int i = 0; i < materialNames.size(); i++) {
            sb.append(i+1).append(". ").append(materialNames.get(i)).append(" (")
              .append(session.getSelectedMaterials().get(materialNames.get(i))).append(")\n");
        }
        
        sb.append("\nEscribe el número del material que deseas modificar");
        sb.append("\nEscribe \"cancelar\" para volver atrás");
        
        sendMessage(chatId, sb.toString());
        session.setUpdatingMaterials(true);
    }

    private void updateReservation(long chatId) {
    UserSession session = userSessions.get(chatId);
    
    // Validar horarios
    if (session.getEndTime().isBefore(session.getStartTime())) {
        sendMessage(chatId, "❌ Error: La hora de fin debe ser posterior a la hora de inicio");
        return;
    }
    
    int labId = getLabIdByName(session.getLabName());
    if (labId == -1) {
        sendMessage(chatId, "❌ Error: Laboratorio no encontrado");
        return;
    }
    
    try (Connection conn = DatabaseUtil.getConnection()) {
        conn.setAutoCommit(false); // Iniciar transacción
        
        try {
            // Actualizar reserva principal
            String updateQuery = "UPDATE Reservas SET " +
                                "Nro_Laboratorio = ?, " +
                                "fecha_reserva = ?, " +
                                "hora_inicio = ?, " +
                                "hora_fin = ?, " +
                                "materia = ?, " +
                                "usa_material_adicional = ? " +
                                "WHERE Id_Reserva = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, labId);
                stmt.setDate(2, Date.valueOf(session.getDate()));
                stmt.setTime(3, Time.valueOf(session.getStartTime()));
                stmt.setTime(4, Time.valueOf(session.getEndTime()));
                stmt.setString(5, session.getSubject());
                stmt.setBoolean(6, !session.getSelectedMaterials().isEmpty());
                stmt.setInt(7, session.getEditingReservationId());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    sendMessage(chatId, "❌ Error al actualizar la reserva");
                    return;
                }
            }
            
            // Eliminar materiales antiguos
            try (PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM Reserva_Material_Adicional WHERE Id_Reserva = ?")) {
                stmt.setInt(1, session.getEditingReservationId());
                stmt.executeUpdate();
            }
            
            // Insertar nuevos materiales si los hay
            if (!session.getSelectedMaterials().isEmpty()) {
                insertReservationMaterials(conn, session.getEditingReservationId(), labId, 
                                         session.getSelectedMaterials());
            }
            
            conn.commit();
            sendMessage(chatId, "✅ Reserva #" + session.getEditingReservationId() + " actualizada exitosamente");
            
            // Limpiar sesión de edición
            session.setEditingReservation(false);
            session.setEditingReservationId(-1);
            session.clearSelectedMaterials();
            
            showMainMenu(chatId);
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    } catch (SQLException e) {
        e.printStackTrace();
        sendMessage(chatId, "❌ Error al actualizar la reserva: " + e.getMessage());
    }
}

    private void showUserReservationsForCancel(long chatId, String username) {
        String query = "SELECT r.Id_Reserva, l.nombre, r.fecha_reserva, r.hora_inicio, r.hora_fin " +
                      "FROM Reservas r " +
                      "JOIN Laboratorios l ON r.Nro_Laboratorio = l.Id_Laboratorio " +
                      "JOIN Usuarios u ON r.id_usuario = u.id_usuario " +
                      "WHERE u.username = ? AND r.fecha_reserva >= CURDATE() AND r.estado = 'Pendiente' " +
                      "ORDER BY r.fecha_reserva, r.hora_inicio";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            
            boolean hasReservations = false;
            while (rs.next()) {
                hasReservations = true;
                int reservationId = rs.getInt("Id_Reserva");
                String labName = rs.getString("nombre");
                Date date = rs.getDate("fecha_reserva");
                Time startTime = rs.getTime("hora_inicio");
                Time endTime = rs.getTime("hora_fin");
                
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(labName + " - " + date + " " + startTime + "-" + endTime);
                button.setCallbackData("cancel_reservation_" + reservationId);
                
                keyboard.add(Collections.singletonList(button));
            }
            
            if (hasReservations) {
                markup.setKeyboard(keyboard);
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("❌ Selecciona la reserva que deseas cancelar:");
                message.setReplyMarkup(markup);
                execute(message);
            } else {
                sendMessage(chatId, "No tienes reservas pendientes que puedas cancelar.");
            }
        } catch (SQLException | TelegramApiException e) {
            e.printStackTrace();
            sendMessage(chatId, "❌ Error al cargar tus reservas. Por favor intenta más tarde.");
        }
    }

    private void handleCancelReservation(long chatId, String callbackData) {
        int reservationId = Integer.parseInt(callbackData.split("_")[2]);
        
        String query = "UPDATE Reservas SET estado = 'Cancelado' WHERE Id_Reserva = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, reservationId);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                sendMessage(chatId, "✅ Reserva #" + reservationId + " cancelada exitosamente.");
            } else {
                sendMessage(chatId, "❌ No se pudo cancelar la reserva. Verifica el ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendMessage(chatId, "❌ Error al cancelar la reserva.");
        }
    }

    private int getLabIdByName(String labName) {
        String query = "SELECT Id_Laboratorio FROM Laboratorios WHERE nombre = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, labName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("Id_Laboratorio");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getMaterialIdByName(String name, int labId) {
        String query = "SELECT N_Objeto FROM Material_Adicional WHERE nombre_objeto = ? AND Id_Laboratorio = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, name);
            stmt.setInt(2, labId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("N_Objeto");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getUserIdByUsername(String username) {
        String query = "SELECT id_usuario FROM Usuarios WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id_usuario");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void sendMessage(long chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(text);
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "Gestor_Labs_bot";
    }

    @Override
    public String getBotToken() {
        return "7902664962:AAGt8DwDo6m5fUy4K2n_Lcl8VnQPKcLndKs";
    }

    private static class UserSession {
    private boolean authenticated = false;
    private String username;
    private String labName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String subject;
    
    private boolean waitingForMaterialResponse = false;
    private boolean waitingForConfirmation = false;
    private boolean addingMaterials = false;
    private boolean editingMaterials = false;
    private boolean removingMaterials = false;
    private boolean updatingMaterials = false;
    private boolean updatingQuantity = false;
    private boolean editingReservation = false;
    private int editingReservationId = -1;
    private String currentMaterialToUpdate;
    private boolean noMaterialsAvailable = false;
    private boolean waitingForAnotherRemoval = false; // Added field
    
    private List<MaterialAdicional> availableMaterials = new ArrayList<>();
    private Map<String, Integer> selectedMaterials = new HashMap<>();

    // Getters and setters
    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getLabName() { return labName; }
    public void setLabName(String labName) { this.labName = labName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public boolean isWaitingForMaterialResponse() { return waitingForMaterialResponse; }
    public void setWaitingForMaterialResponse(boolean waitingForMaterialResponse) { this.waitingForMaterialResponse = waitingForMaterialResponse; }
    public boolean isWaitingForConfirmation() { return waitingForConfirmation; }
    public void setWaitingForConfirmation(boolean waitingForConfirmation) { this.waitingForConfirmation = waitingForConfirmation; }
    public boolean isAddingMaterials() { return addingMaterials; }
    public void setAddingMaterials(boolean addingMaterials) { this.addingMaterials = addingMaterials; }
    public boolean isEditingMaterials() { return editingMaterials; }
    public void setEditingMaterials(boolean editingMaterials) { this.editingMaterials = editingMaterials; }
    public boolean isRemovingMaterials() { return removingMaterials; }
    public void setRemovingMaterials(boolean removingMaterials) { this.removingMaterials = removingMaterials; }
    public boolean isUpdatingMaterials() { return updatingMaterials; }
    public void setUpdatingMaterials(boolean updatingMaterials) { this.updatingMaterials = updatingMaterials; }
    public boolean isUpdatingQuantity() { return updatingQuantity; }
    public void setUpdatingQuantity(boolean updatingQuantity) { this.updatingQuantity = updatingQuantity; }
    public boolean isEditingReservation() { return editingReservation; }
    public void setEditingReservation(boolean editingReservation) { this.editingReservation = editingReservation; }
    public int getEditingReservationId() { return editingReservationId; }
    public void setEditingReservationId(int editingReservationId) { this.editingReservationId = editingReservationId; }
    public String getCurrentMaterialToUpdate() { return currentMaterialToUpdate; }
    public void setCurrentMaterialToUpdate(String currentMaterialToUpdate) { this.currentMaterialToUpdate = currentMaterialToUpdate; }
    public List<MaterialAdicional> getAvailableMaterials() { return availableMaterials; }
    public void setAvailableMaterials(List<MaterialAdicional> availableMaterials) { this.availableMaterials = availableMaterials; }
    public Map<String, Integer> getSelectedMaterials() { return selectedMaterials; }
    
    public void addSelectedMaterial(String name, int quantity) { 
        selectedMaterials.merge(name, quantity, Integer::sum); 
    }
    
    public void clearSelectedMaterials() { 
        selectedMaterials.clear(); 
    }
    
    public boolean isNoMaterialsAvailable() { return noMaterialsAvailable; }
    public void setNoMaterialsAvailable(boolean noMaterialsAvailable) { this.noMaterialsAvailable = noMaterialsAvailable; }
    
    public boolean isWaitingForAnotherRemoval() { return waitingForAnotherRemoval; } // Added getter
    public void setWaitingForAnotherRemoval(boolean waitingForAnotherRemoval) { this.waitingForAnotherRemoval = waitingForAnotherRemoval; } // Added setter
}
    

    private static class MaterialAdicional {
        private String name;
        private int quantity;

        public MaterialAdicional(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
    }
}