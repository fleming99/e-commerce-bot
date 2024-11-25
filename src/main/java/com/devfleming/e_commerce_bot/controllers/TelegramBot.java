package com.devfleming.e_commerce_bot.controllers;

import com.devfleming.e_commerce_bot.domain.dto.ProductDto;
import com.devfleming.e_commerce_bot.domain.entities.Product;
import com.devfleming.e_commerce_bot.domain.usecases.ProductService;
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

import java.util.ArrayList;
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

    private ProductDto productDto = new ProductDto();

    private Map<Long, String> userStates = new HashMap<>();

    public TelegramBot(String botName, String token) {
        super(token);
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String userMessage = message.getText();
            SendMessage messageResponse = new SendMessage();
            messageResponse.setChatId(chatId);
            messageResponse.setParseMode(ParseMode.HTML);

            // Lógica para responder com base na mensagem do usuário
            String responseMessage;

            // Verifica o estado atual do usuário
            String currentState = userStates.getOrDefault(chatId, "START");

            // Dependendo do estado, o bot reage de maneira diferente
            switch (currentState) {
                case "START":
                    responseMessage = "Olá! Selecione uma das opções abaixo";
                    messageResponse.setText(responseMessage);
                    messageResponse.setReplyMarkup(replyKeyboardMarkup(chatId));
                    userStates.put(chatId, "MENU");
                    break;
                case "MENU":
                    switch (userMessage){
                        case "Cadastrar Produto":
                            responseMessage = "Qual é o nome do produto?";
                            messageResponse.setText(responseMessage);
                            userStates.put(chatId,"PRODUCT NAME");
                            break;
                        case "Listar Produtos":
                            List<Product> productList = productService.fetchProductsList();
                            if (productList.isEmpty()){
                                responseMessage = "Não existe nenhum produto cadastrado!";
                                messageResponse.setText(responseMessage);
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
                            messageResponse.setText(String.valueOf(stringBuilder));
                            messageResponse.setReplyMarkup(replyKeyboardMarkup(chatId));
                            userStates.put(chatId,"MENU");
                            break;
                        case "Procurar Produto":
                            responseMessage = "Digite o ID do produto que procura.";
                            messageResponse.setText(responseMessage);
                            userStates.put(chatId, "WAITING PRODUCT ID");
                            break;
                        default:
                            responseMessage = "Não entendi o que você disse. Tente novamente.";
                            messageResponse.setText(responseMessage);
                            messageResponse.setReplyMarkup(replyKeyboardMarkup(chatId));
                            userStates.put(chatId, "MENU");
                            break;
                    }
                    break;
                case "PRODUCT NAME":
                    productDto.setProductName(userMessage);
                    responseMessage = "Dê uma descrição do seu produto.";
                    messageResponse.setText(responseMessage);
                    userStates.put(chatId, "PRODUCT DESCRIPTION");
                    break;
                case "PRODUCT DESCRIPTION":
                    productDto.setProductDescription(userMessage);
                    responseMessage = "E qual o tipo de produto que está sendo cadastrado?";
                    messageResponse.setText(responseMessage);
                    userStates.put(chatId, "PRODUCT TYPE");
                    break;
                case "PRODUCT TYPE":
                    productDto.setProductType(userMessage);
                    Product product = productService.createNewProduct(productDto);
                    if (product != null){
                        responseMessage = "Seu produto foi salvo com sucesso.";
                        messageResponse.setText(responseMessage);
                        messageResponse.setReplyMarkup(replyKeyboardMarkup(chatId));
                        userStates.put(chatId, "MENU");
                    }else {
                        responseMessage = "Houve uma falha durante a persistência. Por favor, tente novamente ou contate os desenvolvedores.";
                        messageResponse.setText(responseMessage);
                        messageResponse.setReplyMarkup(replyKeyboardMarkup(chatId));
                        userStates.put(chatId, "MENU");
                    }
                    break;
                case "WAITING PRODUCT ID":
                    Product savedProduct = productService.fetchSingleProductById(Long.parseLong(userMessage));
                    messageResponse.setText(String.format("ID:%d\nNome do Produto: %s\nDescrição: %s\nTipo: %s\n\n",
                            savedProduct.getProductId(),
                            savedProduct.getProductName(),
                            savedProduct.getProductDescription(),
                            savedProduct.getProductType()));
                    userStates.put(chatId, "MENU");
                    break;
                default:
                    responseMessage = "Estou aqui para te ajudar!";
                    messageResponse.setText(responseMessage);
                    messageResponse.setReplyMarkup(replyKeyboardMarkup(chatId));
                    userStates.put(chatId, "MENU");  // Reseta o estado
                    break;
            }

            try {
                execute(messageResponse);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public ReplyKeyboardMarkup replyKeyboardMarkup(Long chatId){
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
