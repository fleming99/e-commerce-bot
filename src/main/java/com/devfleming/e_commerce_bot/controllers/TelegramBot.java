package com.devfleming.e_commerce_bot.controllers;

import com.devfleming.e_commerce_bot.domain.dto.ProductDto;
import com.devfleming.e_commerce_bot.domain.dto.UserDto;
import com.devfleming.e_commerce_bot.domain.entities.Product;
import com.devfleming.e_commerce_bot.domain.entities.User;
import com.devfleming.e_commerce_bot.domain.usecases.ProductService;
import com.devfleming.e_commerce_bot.domain.usecases.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.devfleming.e_commerce_bot.mappers.ListMapper.mapListToStringBuilder;

@Slf4j
@Data
@RequiredArgsConstructor
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final String botName;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;
    
    private SendMessage sendMessage = new SendMessage();

    private ProductDto productDto = new ProductDto();

    private UserDto userDto = new UserDto();

    private Map<Long, String> userStates = new HashMap<>();

    public TelegramBot(String botName, String token) {
        super(token);
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {

        setSendMessageConfig(update.getMessage());

        if (update.hasMessage() && update.getMessage().hasText() || update.getMessage().hasContact()) {

            String currentState = userStates.getOrDefault(update.getMessage().getChatId(), "LOGIN");

            processMessage(update.getMessage(), currentState);
        }
    }

    public void processMessage(Message message, String currentState){
        switch (currentState) {
            case "LOGIN":
                handleWelcomeMessage(message);
                break;
            case "WAITING FOR CELLPHONE NUMBER":
                handleLogin(message);
                break;
            case "WAITING FOR CPF":
                handleSecondStepSignup(message);
                break;
            case "MENU":
                handleMenu(message);
                break;
            case "PRODUCT NAME":
                handleNewProductFirstStep(message);
                break;
            case "PRODUCT DESCRIPTION":
                handleNewProductSecondStep(message);
                break;
            case "PRODUCT TYPE":
                handleNewProductThirdStep(message);
                break;
            case "WAITING PRODUCT ID TO FETCH":
                handleFetchProductById(message);
                break;
            case "WAITING PRODUCT ID TO INACTIVATE":
                handleInactivateProductById(message);
                break;
            default:
                handleDefaultMessage(message);
                break;
        }
    }

    public void handleWelcomeMessage(Message message){

        sendMessage.setText("Olá. Deseja compartilhar seu número e nome para fazer o login?");
        sendMessage.setReplyMarkup(loginButtons());
        userStates.put(message.getChatId(), "WAITING FOR CELLPHONE NUMBER");

        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleLogin(Message message){

        if (message.hasContact()) {
            String phoneNumber = message.getContact().getPhoneNumber();
            User user = userService.fetchByCellphone(phoneNumber);

            if (user != null) {
                sendMessage.setText(String.format("Seja bem vindo %s %s!\nSelecione uma das opções abaixo:", user.getFirstName(), user.getLastName()));
                sendMessage.setReplyMarkup(menuButtons());
                userStates.put(message.getChatId(), "MENU");
            } else {
                handleFirstStepSignup(message, phoneNumber);
            }
        } else {
            sendMessage.setText("Para utilizar o bot, você deve compartilhar seu numero de telefone.");
            sendMessage.setReplyMarkup(loginButtons());  // Reenviar o botão para o compartilhamento do número
            userStates.put(message.getChatId(), "WAITING FOR CELLPHONE NUMBER");
        }

        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleReturnToMenu(Message message){
        sendMessage.setText("Selecione uma das opções abaixo:");
        sendMessage.setReplyMarkup(menuButtons());
        userStates.put(message.getChatId(), "MENU");

        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleFirstStepSignup(Message message, String phoneNumber){

        userDto.setFirstName(message.getContact().getFirstName());
        userDto.setLastName(message.getContact().getLastName());
        userDto.setCellphone(phoneNumber);
        sendMessage.setText("Qual é o seu CPF? (Digite apenas números)");
        sendMessage.setReplyMarkup(null);
        userStates.put(message.getChatId(), "WAITING FOR CPF");
    }

    public void handleSecondStepSignup(Message message){

        if (userService.fetchByCpf(message.getText()) == null){
            userDto.setCpf(message.getText());
            userService.createNewUser(userDto);
            sendMessage.setText("Seu usuário foi criado com sucesso!\nSelecione uma das opções abaixo.");
            sendMessage.setReplyMarkup(menuButtons());
            userStates.put(message.getChatId(), "MENU");
        }else {
            sendMessage.setText("Este CPF já está cadastrado em outro número. Tente novamente com outro CPF.");
            sendMessage.setReplyMarkup(loginButtons());
            userStates.put(message.getChatId(), "WAITING FOR CELLPHONE NUMBER");
        }

        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleMenu(Message message){
        switch (message.getText()){
            case "Cadastrar Produto":
                sendMessage.setText("Qual é o nome do produto?");
                userStates.put(message.getChatId(),"PRODUCT NAME");
                break;
            case "Listar Produtos":
                List<Product> productList = productService.fetchProductsList();
                if (productList.isEmpty() || mapListToStringBuilder(productList).isEmpty()){
                    sendMessage.setText("Não existe nenhum produto cadastrado/ativo!");
                    sendMessage.setReplyMarkup(menuButtons());
                    userStates.put(message.getChatId(), "MENU");
                    break;
                }
                sendMessage.setText(String.valueOf(mapListToStringBuilder(productList)));
                sendMessage.setReplyMarkup(menuButtons());
                userStates.put(message.getChatId(), "MENU");
                break;
            case "Procurar Produto":
                sendMessage.setText("Digite o ID do produto que procura.");
                userStates.put(message.getChatId(), "WAITING PRODUCT ID TO FETCH");
                break;
            case "Inativar Produto":
                sendMessage.setText("Digite o ID do produto que deseja inativar.");
                userStates.put(message.getChatId(), "WAITING PRODUCT ID TO INACTIVATE");
                break;
            default:
                sendMessage.setText("Não entendi o que você disse. Tente novamente.");
                sendMessage.setReplyMarkup(menuButtons());
                userStates.put(message.getChatId(), "MENU");
                break;
        }

        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleNewProductFirstStep(Message message){
        productDto.setProductName(message.getText());
        sendMessage.setText("Dê uma descrição do seu produto.");
        sendMessage.setReplyMarkup(null);
        userStates.put(message.getChatId(), "PRODUCT DESCRIPTION");

        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleNewProductSecondStep(Message message){
        productDto.setProductDescription(message.getText());
        sendMessage.setText("E qual o tipo de produto que está sendo cadastrado?");
        sendMessage.setReplyMarkup(null);
        userStates.put(message.getChatId(), "PRODUCT TYPE");

        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleNewProductThirdStep(Message message){
        productDto.setProductType(message.getText());
        Product product = productService.createNewProduct(productDto);
        if (product != null){
            sendMessage.setText("Seu produto foi salvo com sucesso.");
        }else {
            sendMessage.setText("Houve uma falha durante a persistência. Por favor, tente novamente ou contate os desenvolvedores.");
        }
        sendMessage.setReplyMarkup(menuButtons());
        userStates.put(message.getChatId(), "MENU");

        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleFetchProductById(Message message){


        try {
            Product savedProduct = productService.fetchSingleProductById(Long.parseLong(message.getText()));

            if (savedProduct.getActive() == 'A') {
                sendMessage.setText(String.format("ID:%d\nNome do Produto: %s\nDescrição: %s\nTipo: %s\n\n",
                        savedProduct.getProductId(),
                        savedProduct.getProductName(),
                        savedProduct.getProductDescription(),
                        savedProduct.getProductType()));
                userStates.put(message.getChatId(), "MENU");
            }else {
                sendMessage.setText("Não foi possível obter informações sobre este produto, pois ele está inativo.");
                userStates.put(message.getChatId(), "MENU");
            }
            try{
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } catch (NumberFormatException e) {
            sendMessage.setText("Não foi possível encontrar o produto.\nLembre-se de digitar apenas números!");
            try{
                execute(sendMessage);
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }

            handleReturnToMenu(message);
        }
    }

    public void handleInactivateProductById(Message message){

        try {
            Product savedProduct = productService.fetchSingleProductById(Long.parseLong(message.getText()));

            if (savedProduct.getActive() == 'A') {
                productService.inactivateProductById(savedProduct.getProductId());
                sendMessage.setText("Produto inativado com sucesso.");
                userStates.put(message.getChatId(), "MENU");
            }else {
                sendMessage.setText("O produto já está inativo.");
                userStates.put(message.getChatId(), "MENU");
            }
            try{
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } catch (NumberFormatException e) {
            sendMessage.setText("Não foi possível encontrar o produto para inativar.\nLembre-se de digitar apenas números!");
            try{
                execute(sendMessage);
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }

            handleReturnToMenu(message);
        }
    }

    public void handleDefaultMessage(Message message){
        sendMessage.setText("Não entendi a mensagem anterior. Vamos tentar novamente.");
        sendMessage.setReplyMarkup(menuButtons());
        userStates.put(message.getChatId(), "MENU");

        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public ReplyKeyboardMarkup loginButtons(){
        KeyboardButton firstButton = new KeyboardButton("Não");

        KeyboardButton secondButton = new KeyboardButton("Sim");

        secondButton.setRequestContact(true);

        KeyboardRow firstRow = new KeyboardRow(List.of(firstButton, secondButton));

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(firstRow));

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup menuButtons(){
        KeyboardButton firstButton = new KeyboardButton("Cadastrar Produto");

        KeyboardButton secondButton = new KeyboardButton("Listar Produtos");

        KeyboardRow firstRow = new KeyboardRow(List.of(firstButton, secondButton));

        KeyboardButton thirdButton = new KeyboardButton("Procurar Produto");

        KeyboardButton fourthButton = new KeyboardButton("Inativar Produto");

        KeyboardRow secondRow = new KeyboardRow(List.of(thirdButton, fourthButton));

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(firstRow, secondRow));

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }

    public void setSendMessageConfig(Message message){
        sendMessage.setChatId(message.getChatId());
        sendMessage.setParseMode(ParseMode.HTML);
    }
}
