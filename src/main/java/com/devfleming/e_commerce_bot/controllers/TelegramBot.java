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

    private ProductDto productDto = new ProductDto();

    private UserDto userDto = new UserDto();

    private Map<Long, String> userStates = new HashMap<>();

    public TelegramBot(String botName, String token) {
        super(token);
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info(update.toString());
        if (update.hasMessage() && update.getMessage().hasText() || update.getMessage().hasContact()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String userMessage = message.getText();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode(ParseMode.HTML);

            // Lógica para responder com base na mensagem do usuário
            String responseMessage;

            // Verifica o estado atual do usuário
            String currentState = userStates.getOrDefault(chatId, "LOGIN");

            // Dependendo do estado, o bot reage de maneira diferente
            switch (currentState) {
                case "LOGIN":
                    responseMessage = "Olá. Deseja compartilhar seu número e nome para fazer o login?";
                    sendMessage.setText(responseMessage);
                    sendMessage.setReplyMarkup(loginKeyboardMarkup());
                    userStates.put(chatId, "WAITING FOR CELLPHONE NUMBER");
                    break;
                case "WAITING FOR CELLPHONE NUMBER":
                    if (update.getMessage().hasContact()) {
                        String phoneNumber = update.getMessage().getContact().getPhoneNumber();
                        User user = userService.fetchByCellphone(phoneNumber);

                        if (user != null) {
                            responseMessage = String.format("Seja bem vindo %s %s!\nSelecione uma das opções abaixo:", user.getFirstName(), user.getLastName());
                            sendMessage.setText(responseMessage);
                            sendMessage.setReplyMarkup(replyKeyboardMarkup());
                            userStates.put(chatId, "MENU");
                            break;
                        } else {
                            userDto.setFirstName(message.getContact().getFirstName());
                            userDto.setLastName(message.getContact().getLastName());
                            userDto.setCellphone(phoneNumber);
                            responseMessage = "Qual é o seu CPF? (Digite apenas números)";
                            sendMessage.setText(responseMessage);
                            userStates.put(chatId, "WAITING FOR CPF");
                            break;
                        }
                    } else {
                        // Caso o usuário não tenha compartilhado o número, enviar uma mensagem de erro
                        responseMessage = "Para utilizar o bot, você deve compartilhar seu numero de telefone.";
                        sendMessage.setText(responseMessage);
                        sendMessage.setReplyMarkup(loginKeyboardMarkup());  // Reenviar o botão para o compartilhamento do número
                        userStates.put(chatId, "WAITING FOR CELLPHONE NUMBER");
                    }
                    break;
                case "WAITING FOR CPF":
                    if (userService.fetchByCpf(message.getText()) == null){
                        userDto.setCpf(message.getText());
                        userService.createNewUser(userDto);
                        responseMessage = "Seu usuário foi criado com sucesso!\nSelecione uma das opções abaixo.";
                        sendMessage.setText(responseMessage);
                        sendMessage.setReplyMarkup(replyKeyboardMarkup());
                        userStates.put(chatId, "MENU");
                    }else {
                        responseMessage = "Este CPF já está cadastrado em outro número. Tente novamente com outro CPF.";
                        sendMessage.setText(responseMessage);
                        sendMessage.setReplyMarkup(loginKeyboardMarkup());
                        userStates.put(chatId, "WAITING FOR CELLPHONE NUMBER");
                    }
                    break;
                case "MENU":
                    switch (userMessage){
                        case "Cadastrar Produto":
                            responseMessage = "Qual é o nome do produto?";
                            sendMessage.setText(responseMessage);
                            userStates.put(chatId,"PRODUCT NAME");
                            break;
                        case "Listar Produtos":
                            List<Product> productList = productService.fetchProductsList();
                            if (productList.isEmpty()){
                                responseMessage = "Não existe nenhum produto cadastrado!";
                                sendMessage.setText(responseMessage);
                                userStates.put(chatId, "START");
                                break;
                            }
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Product product : productList){
                                stringBuilder.append(String.format("ID:%d\nNome do Produto: %s\nDescrição: %s\nTipo: %s\n\n",
                                        product.getProductId(),
                                        product.getProductName(),
                                        product.getProductDescription(),
                                        product.getProductType()));
                            }
                            sendMessage.setText(String.valueOf(stringBuilder));
                            sendMessage.setReplyMarkup(replyKeyboardMarkup());
                            userStates.put(chatId,"MENU");
                            break;
                        case "Procurar Produto":
                            responseMessage = "Digite o ID do produto que procura.";
                            sendMessage.setText(responseMessage);
                            userStates.put(chatId, "WAITING PRODUCT ID");
                            break;
                        default:
                            responseMessage = "Não entendi o que você disse. Tente novamente.";
                            sendMessage.setText(responseMessage);
                            sendMessage.setReplyMarkup(replyKeyboardMarkup());
                            userStates.put(chatId, "MENU");
                            break;
                    }
                    break;
                case "PRODUCT NAME":
                    productDto.setProductName(userMessage);
                    responseMessage = "Dê uma descrição do seu produto.";
                    sendMessage.setText(responseMessage);
                    userStates.put(chatId, "PRODUCT DESCRIPTION");
                    break;
                case "PRODUCT DESCRIPTION":
                    productDto.setProductDescription(userMessage);
                    responseMessage = "E qual o tipo de produto que está sendo cadastrado?";
                    sendMessage.setText(responseMessage);
                    userStates.put(chatId, "PRODUCT TYPE");
                    break;
                case "PRODUCT TYPE":
                    productDto.setProductType(userMessage);
                    Product product = productService.createNewProduct(productDto);
                    if (product != null){
                        responseMessage = "Seu produto foi salvo com sucesso.";
                        sendMessage.setText(responseMessage);
                        sendMessage.setReplyMarkup(replyKeyboardMarkup());
                        userStates.put(chatId, "MENU");
                    }else {
                        responseMessage = "Houve uma falha durante a persistência. Por favor, tente novamente ou contate os desenvolvedores.";
                        sendMessage.setText(responseMessage);
                        sendMessage.setReplyMarkup(replyKeyboardMarkup());
                        userStates.put(chatId, "MENU");
                    }
                    break;
                case "WAITING PRODUCT ID":
                    Product savedProduct = productService.fetchSingleProductById(Long.parseLong(userMessage));
                    sendMessage.setText(String.format("ID:%d\nNome do Produto: %s\nDescrição: %s\nTipo: %s\n\n",
                            savedProduct.getProductId(),
                            savedProduct.getProductName(),
                            savedProduct.getProductDescription(),
                            savedProduct.getProductType()));
                    userStates.put(chatId, "MENU");
                    break;
                default:
                    responseMessage = "Estou aqui para te ajudar!";
                    sendMessage.setText(responseMessage);
                    sendMessage.setReplyMarkup(replyKeyboardMarkup());
                    userStates.put(chatId, "MENU");  // Reseta o estado
                    break;
            }

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void processMessage(Message message){

    }

    public ReplyKeyboardMarkup loginKeyboardMarkup(){
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

    public ReplyKeyboardMarkup replyKeyboardMarkup(){
        KeyboardButton firstButton = new KeyboardButton("Cadastrar Produto");

        KeyboardButton secondButton = new KeyboardButton("Listar Produtos");

        KeyboardRow firstRow = new KeyboardRow(List.of(firstButton, secondButton));

        KeyboardButton thirdButton = new KeyboardButton("Procurar Produto");

        KeyboardButton fourthButton = new KeyboardButton("Invativar Produto");

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
}
