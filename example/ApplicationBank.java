package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.TextFormat;

import javax.persistence.*;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ApplicationBank {
    static EntityManagerFactory emf; //ссылочная переменная для коннекшина
    static EntityManager em; //ссылочная переменная для управления сущностями 

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in); //поток для ввода данных с клавиатуры
        try {

            emf = Persistence.createEntityManagerFactory("JPATest4"); //выполняем соединение
            em = emf.createEntityManager(); //инициализируем обьект для управления сущностями

            //заполним сразу таблицу с курсом валют тремя позициями и истановим им сразу курс по отношению к гривне
            //далее можно будет через меню добавлять валюту в таблицу и устанавливать курс
            em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями
//            RateOfExchange rateOfExchange1 = new RateOfExchange("USD", 36.56); //доллар - ручной ввод курса
            double rateUSD = usdInfoGetFromPrivatBankAPI("10.02.2023"); //курс доллара полученныйы чрез API ПриватБанка на необходимое число
            RateOfExchange rateOfExchange1 = new RateOfExchange("USD", rateUSD); //доллар
            em.persist(rateOfExchange1);
            RateOfExchange rateOfExchange2 = new RateOfExchange("EUR", 39.83); //евро
            em.persist(rateOfExchange2);
            RateOfExchange rateOfExchange3 = new RateOfExchange("UAH", 1.0); //гривна
            em.persist(rateOfExchange3);
            em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями

            try {
                while (true) {
                    System.out.println("1: add client"); //добавить клиента
                    System.out.println("2: delete client"); //удалить клинента
                    System.out.println("3: view clients"); //просмотреть всех клиентов
                    System.out.println("4: view transactions"); //просмотреть транзакции
                    System.out.println("5: add account"); //создание счета с первым пополнением
                    System.out.println("6: top up account"); //пополнить счет
                    System.out.println("7: delete account"); //удалить счет
                    System.out.println("8: view accounts"); //просмотреть все счета
                    System.out.println("9: add rate of exchange"); //добавить валюту с ее курсом
                    System.out.println("10: delete rate of exchange"); //удалить валюту с ее курсом
                    System.out.println("11: view rates of exchange"); //просмотреть все валюты с их курсом
                    System.out.println("12: transfer funds"); //переводить средства
                    System.out.println("13: transfer funds with conversion"); //переводить средства с конвертацией
                    System.out.println("14: transfer funds with conversion for single Client"); //переводить средства с конвертацией
                    System.out.println("15: view total funds single client in UAH"); //просмотреть суммарно средства одного клиента в гривне

                    System.out.print("-> ");

                    String str = sc.nextLine();
                    switch (str) { //блок для общения с пользователем (меню нашего приложения)
                        case "1":
                            addClient(sc);
                            break;
                        case "2":
                            deleteClient(sc);
                            break;
                        case "3":
                            viewClients();
                            break;
                        case "4":
                            viewTransactions();
                            break;
                        case "5":
                            addAccount(sc);
                            break;
                        case "6":
                            topUpAccount(sc);
                            break;
                        case "7":
                            deleteAccount(sc);
                            break;
                        case "8":
                            viewAccounts();
                            break;
                        case "9":
                            addRateOfExchange(sc);
                            break;
                        case "10":
                            deleteRateOfExchange(sc);
                            break;
                        case "11":
                            viewRateOfExchanges();
                            break;
                        case "12":
                            transferFunds(sc);
                            break;
                        case "13":
                            transferFundsWithConversion(sc);
                            break;
                        case "14":
                            transferFundsWithConversionForSingleClient(sc);
                            break;
                        case "15":
                            totalFundsSingleClientInUAH(sc);
                            break;

                        default:
                            return;
                    }
                }
            } finally { //блок для закрытия потоков и соединений
                sc.close();
                em.close();
                emf.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static double usdInfoGetFromPrivatBankAPI(String today) { //метод для получения курса доллара из ПриватБанка
//        Gson gson = new GsonBuilder().create();
        String json = ""; //оздаем строку в которую запишем прочитанное с API банка
        double rateOfExchangeToUSD = 0.0; //переменная для хранения курса доллара в гривне
        try {
            URL url = new URL("https://api.privatbank.ua/p24api/exchange_rates?json&date=" + today); //формируем URL-адрес
            //аргументом для метода выступает дата для отображения курса
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(); //настраиваем соединение (иначе будет
            // приравниваться пустому(без тела) GET-запросу

            try (InputStream inputStream = httpConnection.getInputStream()) { //в блоке try-with-resource через поток ввода данных получаем
                //тело ответа от сервера
                byte[] buffer = responseBodyToArray(inputStream); //в буфер(байтовый массив) из стрима считываем данные, с помощью
                //специального метода-конвертера

                json = new String(buffer, StandardCharsets.UTF_8); //считанное из стрима в буфер преобразовываем
                //в строку по стандарту StandardCharsets.UTF_8 (восьмиразрядный формат преобразования UCS)

                String[] rate = json.split("\"baseCurrency\":"); //создаем строковый массив = данные, полученные из GET JSON
                //Приват Банка, разбиваем на элементы массива (с помощью метода split(аргумент для регулярного выражения)
                for (int i = 0; i < rate.length; i++) { //проходимся по массиву из строк после разбития
                    if (rate[i].contains("\"UAH\",\"currency\":\"USD\"")) { //если есть совпадение, то
                        json = rate[i].substring(rate[i].indexOf("\"saleRate\":") + 11, rate[i].indexOf("0000,\"purchaseRate\":")); //вытаскиваем
                        //курс доллара в гривне (берем элемент строки(подстроку) от фразы "saleRate:" + 11символов9чтоб выйти на число) и до
                        //фразы "0000,"purchaseRate:"
                        rateOfExchangeToUSD = Double.parseDouble(json); //парсим полученную подстроку в число
                    }
                }
            }
        } catch (IOException e) { //ловим ошибки при вводе/выводе данных
            e.printStackTrace();
        }
        return rateOfExchangeToUSD; //возвращаем курс доллара к гривне
    }
    private static byte[] responseBodyToArray(InputStream is) throws IOException { //специальный метод по преобразованию данных
        //из стрима (InputStream is) в байтовый массив
        ByteArrayOutputStream bos = new ByteArrayOutputStream(); //создаем стрим для вывода данных с большим допустимым обьемом
        byte[] buf = new byte[10240]; //создаем буфер - байтовый массив с определенным размером (10 килоБайт)
        int r; //целочисленная переменная для хранения прочитанных байтов с помощью метода read()

        do { //запускаем цикл, который выполнится хотябы один раз
            r = is.read(buf); //из InputStream is читаем в буфер столько, сколько поместиться (read передает в r сколько
            // прочел байтов)
            if (r > 0)
                bos.write(buf, 0, r); //если было что-то прочитано, то записываем это в поток вывода ByteArrayOutputStream bos
        } while (r != -1); //пока стрим не закончится(т.е. есть данные для чтения) - это условие выхода из цикла

        return bos.toByteArray(); //после чего возвращаем все, что накопилось в ByteArrayOutputStream bos, преобразовав все
        //в массив байтов
    }

    private static void addClient(Scanner sc) { //метод для добавления клиента в таблицу
        System.out.print("Enter client name: "); //просим ввести имя клиента
        String name = sc.nextLine(); //читаем введенное имя

        System.out.print("Enter client phone: "); //просим ввести номер телефона
        String strPhone = sc.nextLine(); //читаем введенный номер телефона
        int phone = Integer.parseInt(strPhone); //парсим номер телефона в число

        em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями //запускаем транзакцию
        try {
            Client client = new Client(name, phone); //оздаем нового клиента с аргументами для параметров конструктора
            em.persist(client); //помещаем сущность к контекст
            em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями

            System.out.println(client.getId());
        } catch (Exception exception) {
            em.getTransaction().rollback(); //если возникла ошибка - отменяем все действия в изменениях сущностей
        }
    }

    private static void deleteClient(Scanner sc) { //метод для удаления клиента из таблицы
        System.out.print("Enter client id: "); //просим пользователя ввести id клиента
        String strId = sc.nextLine(); //читаем с консоли id клиента
        long id = Long.parseLong(strId); //парсим id клиента в число

        Client client = em.getReference(Client.class, id); //выполняем поиск по id
        if (client == null) { //если нет
            System.out.println("Client not found!"); //печатаем - клиент не найден
            return; //вернуться в меню
        }

        em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями
        try {
            em.remove(client); //удаляем клиента
            em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями
        } catch (Exception ex) {
            em.getTransaction().rollback(); //если возникла ошибка - отменяем все действия в изменениях сущностей
        }
    }

    private static void viewClients() { //метод для демонстрации всех клиентов из таблицы
        String queryClients = "SELECT c FROM Client c"; //строка-запрос для таблицы Клиент
        Query query = em.createQuery(queryClients, Client.class); //запрос
        List<Client> list = (List<Client>) query.getResultList(); //создаем список для результатов поиска

        for (Client client : list) //для каждого клиента из списка
            System.out.println(client); //выводим на консоль
    }

    private static void viewTransactions() { //метод для просмотра всех транзакций
        String queryTransactions = "SELECT t FROM Transaction t"; //строка запрос для поиска в таблице Транзакции
        Query query = em.createQuery(queryTransactions, Transaction.class); //запрос
        List<Transaction> list = (List<Transaction>) query.getResultList(); //создаем список для результатов поиска

        for (Transaction transaction : list) { //для каждой транзакции из списка
            System.out.println(transaction); //выводим на консоль
        }
    }

    private static void addAccount(Scanner sc) { //метод для создания счета по имени Клиента (с первым пополнением)
        System.out.print("Enter client name: "); //просим ввести имя клиента
        String clientName = sc.nextLine(); //читаем с консоли имя клиента

        Client client = null; //создаем ссыл.переменную типа класса Клиент
        try {
            String queryNameClient = "SELECT c FROM Client c WHERE c.name = :name"; //строка-запрос
            Query query = em.createQuery(queryNameClient); //создаем запрос
            query.setParameter("name", clientName); //заменяем имя на параметр(введенное имя с клавиатуры)
            client = (Client) query.getSingleResult(); //в ссыл.переменную кладем одиночный результат от запроса


            System.out.print("Enter sum: "); //просим ввести сумму, которую кладем на счет
            String strSum = sc.nextLine(); //читаем сумму
            double sum = Double.parseDouble(strSum); //парсим сумму в число

            em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями
            Account account = new Account(selectCurrency(), sum, client); //создаем новый счет с аргументами для параметров

            em.persist(account); //сохраняем обьект (счет)
            em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями
            System.out.println("Ok.");

            //тут два исключения, которые обязательны для перехвата из-за метода getSingleResult()
        } catch (NoResultException exception) {
            System.out.println("Client not found!");
            return;
        } catch (NonUniqueResultException exception) {
            System.out.println("Non unique client found!");
            return;
        }
    }

    private static void topUpAccount(Scanner sc) { //метод для пополнения счета
        double thisBalance = 0.0; //создаем переменную для суммы пополнения
        System.out.print("Enter account id: "); //просим ввести id счета
        String strAccountId = sc.nextLine(); //читаем с консоли id счета
        long accountId = Long.parseLong(strAccountId); //парсим id счета

        Account account = em.find(Account.class, accountId); //выполняем поиск по id счета
        if (account == null) { //если нет, то
            System.out.println("Account not found!"); //счет не найден
            return; //вернуться в меню
        }

        System.out.print("Enter sum: "); //просим ввести сумму пополнения
        String strSum = sc.nextLine(); //читаем с консоли сумму пополнения
        double balance = Double.parseDouble(strSum); //парсим сумму пополнения

        em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями
        try {
            Query query; //ссылка на запрос
            String queryCurrency = "SELECT r FROM RateOfExchange r WHERE r.currency = :currency"; //строка-запрос в таблице
            //курса валют по названию столбца валюта

            if (!selectCurrency().equals(account.getCurrency())) { //если валюта из счета не равна валюте из метода по возврату валюту, то
                query = em.createQuery(queryCurrency); //запрос
                query.setParameter("currency", account.getCurrency()); //подменяем строку на параметр
                RateOfExchange rateOfExchange = (RateOfExchange) query.getSingleResult(); //получаем одиночный результат
                double balanceOfAccount = rateOfExchange.getRateToUAH();

                if (selectCurrency().equals("UAH")) {
                    thisBalance = (balance / balanceOfAccount);
                } else {
                    thisBalance = balance;
                }
                account.replenishBalance(thisBalance);
                em.persist(account); //сохраняем обьект
            } else {
                thisBalance = balance;
                account.replenishBalance(thisBalance);
                em.persist(account); //сохраняем обьект
            }

            Transaction transaction = new Transaction(account.getClient(), account, null, thisBalance); //для пополнения
            // счета из терминалов
            em.persist(transaction); //сохраняем обьект
            em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями
            System.out.println("Ok!");
        } catch (Exception exception) {
            exception.printStackTrace();
            em.getTransaction().rollback(); //если возникла ошибка - отменяем все действия в изменениях сущностей
            return; //вернуться в меню
        }
    }

    private static void deleteAccount(Scanner sc) { //метод для удаления счета из таблицы
        System.out.print("Enter account id: "); //просим ввести id счета
        String strId = sc.nextLine(); //читаем с консоли id счета
        long id = Long.parseLong(strId); //парсим id счета

        Account account = em.getReference(Account.class, id); //выполняем поиск по id счета
        if (account == null) { //если нет
            System.out.println("Account not found!"); //пишем - счет не найден
            return; //вернуться в меню
        }

        em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями
        try {
            em.remove(account); //удаляем обьект
            em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями
        } catch (Exception ex) {
            em.getTransaction().rollback(); //если возникла ошибка - отменяем все действия в изменениях сущностей
        }
    }

    private static void viewAccounts() { //метод для демонстрации всех счетов из таблицы
        String queryAccounts = "SELECT a FROM Account a"; //строка-запрос для поиска в таблице счетов
        Query query = em.createQuery(queryAccounts, Account.class); //запрос
        List<Account> list = (List<Account>) query.getResultList(); //создаем список для результатов поиска

        for (Account account : list) //для каждого счета из списка
            System.out.println(account); //вывести на экран счет
    }

    private static void addRateOfExchange(Scanner sc) { //метод для добавления курса валют в таблицу
        System.out.print("Enter currency: "); //просим ввести валюту
        String currency = sc.nextLine(); //читаем с консоли валюту

        System.out.print("Enter rate to UAH: "); //просим ввести курс по отношению к гривне
        String strRateToUAH = sc.nextLine(); //читаем с консоли веденный курс к гривне
        double rateToUAH = Double.parseDouble(strRateToUAH); //парсим курс к гривне

        em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями
        try {
            RateOfExchange rateOfExchange = new RateOfExchange(currency, rateToUAH); //создаем новый курс валют с аргументами для параметров
            em.persist(rateOfExchange); //сохраняем обьект
            em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями

            System.out.println(rateOfExchange.getId()); //печатаем id курса валют
        } catch (Exception exception) {
            em.getTransaction().rollback(); //если возникла ошибка - отменяем все действия в изменениях сущностей
        }
    }

    private static void deleteRateOfExchange(Scanner sc) { //метод для удаления клиента из таблицы
        System.out.print("Enter rate of exchange id: "); //просим ввести id курса валют
        String strId = sc.nextLine(); //читаем id курса валют введенный пользоватеем
        long id = Long.parseLong(strId); //парсим id курса валют

        RateOfExchange rateOfExchange = em.getReference(RateOfExchange.class, id); //выполняем поиск по id курса валют
        if (rateOfExchange == null) { //если нет, то
            System.out.println("Rate of exchange not found!"); //пишем курс валют не найден
            return; //вернуться в меню
        }

        em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями
        try {
            em.remove(rateOfExchange); //удаляем обьект
            em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями
        } catch (Exception ex) {
            em.getTransaction().rollback(); //если возникла ошибка - отменяем все действия в изменениях сущностей
        }
    }

    private static void viewRateOfExchanges() { //метод для демонстрации всех клиентов из таблицы
        String queryRate = "SELECT r FROM RateOfExchange r"; //строка-запрос для поиска в таблице курс валют
        Query query = em.createQuery(queryRate, RateOfExchange.class); //запрос
        List<RateOfExchange> list = (List<RateOfExchange>) query.getResultList(); //создаем список для результатов запроса

        for (RateOfExchange rateOfExchange : list) //для каждого курса валют из списка
            System.out.println(rateOfExchange); //выводим на печать
    }

    private static void transferFunds(Scanner sc) { //метод для перевода денег между счетами с одинаковой валютой
        System.out.print("Enter your sender account id: "); //просим ввести id счета-отправителя
        String strYourSenderAccountId = sc.nextLine(); //читаем id счета-отправителя
        Long yourSenderAccountId = Long.parseLong(strYourSenderAccountId); //парсим id счета-отправителя

        Account senderAccount = em.find(Account.class, yourSenderAccountId); //выполняем поиск по id счета-отправителя
        if (senderAccount == null) { //если нет
            System.out.println("Account not found!"); //то пишем - счет не найден
            return; //вернуться в меню
        }

        System.out.print("Enter beneficiary account id: "); //просим ввести id счета-получателя
        String strBeneficiaryAccountId = sc.nextLine(); //считываем id счета-получателя
        Long beneficiaryAccountId = Long.parseLong(strBeneficiaryAccountId); //парсим id счет-получателя в число

        Account beneficiaryAccount = em.find(Account.class, beneficiaryAccountId);
        if (beneficiaryAccount == null) { //если счет-получатель отсутствует, то
            System.out.println("Account not found!"); //пишем - счет не найден
            return; //вернуться в меню
        }

        if (senderAccount.getCurrency().equals(beneficiaryAccount.getCurrency())) { //если валюта счета-отправителя равна
            //валюте счета-получателя, то
            System.out.print("Enter sum for transfer: "); //просим ввести сумму для перевода
            String strSum = sc.nextLine(); //читаем сумму введенную пользователем
            double sum = Double.parseDouble(strSum); //парсим сумму в число
            if (sum > senderAccount.getBalance()) { //если введенная сумма больше чем баланс счета, то
                System.out.println("Error! Insufficiently money!"); //пишем - Ошибка! Недостаточно денег!
                return; //вернуться в меню
            }

            em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями
            try {
                Transaction transaction = new Transaction(senderAccount.getClient(), beneficiaryAccount, senderAccount, sum);
                em.persist(transaction); //сохраняем обьект

                beneficiaryAccount.replenishBalance(sum); //к счету-получателю добавляем сумму
                senderAccount.withdrawFromBalance(sum); //от счета отправителя отнимаем сумму

                em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями
                System.out.println("Ok!");
            } catch (Exception exception) {
                exception.printStackTrace();
                em.getTransaction().rollback(); //если возникла ошибка - отменяем все действия в изменениях сущностей
                return;
            }
        } else {
            System.out.println("Enter the account with the currency that matches!"); //печатаем - введите счет с валютой, которая
            //соответствует
            return; //вернуться в меню
        }
    }

    private static void transferFundsWithConversion(Scanner sc) { //метод для перевода средств с адаптивной конвертацией валюты
        System.out.print("Enter sender account id: "); //просим ввести id счета-отправителя
        String strSenderAccountId = sc.nextLine(); //считываем id счета-отправителя
        Long senderAccountId = Long.parseLong(strSenderAccountId); //парсим id счет-отправителя в число

        Account senderAccount = em.find(Account.class, senderAccountId); //находим счет-отправитель по его id
        if (senderAccount == null) { //если счет-отправитель отсутствует, то
            System.out.println("Account not found!"); //пишем - счет не найден
            return; //вернуться в меню
        }

        System.out.print("Enter beneficiary account id: "); //просим ввести id счета-получателя
        String strBeneficiaryAccountId = sc.nextLine(); //считываем id счета-получателя
        Long beneficiaryAccountId = Long.parseLong(strBeneficiaryAccountId); //парсим id счет-получателя в число

        Account beneficiaryAccount = em.find(Account.class, beneficiaryAccountId); //находим счет-получатель по его id
        if (beneficiaryAccount == null) { //если счет-получатель отсутствует, то
            System.out.println("Account not found!"); //пишем - счет не найден
            return; //вернуться в меню
        }

        System.out.print("Enter sum for transfer: "); //просим ввести сумму для перевода между счетами
        String strSum = sc.nextLine(); //читаем ответ пользователя
        double sum = Double.parseDouble(strSum); //парсим переменную - сумма средств для перевода
        if (sum > senderAccount.getBalance()) { //если сумма перевода больше суммы на счете-отправителе, то
            System.out.println("Error! Insufficiently money!"); //Ошибка! Недостаточно средств!
            return; //возвращаемся в меню
        }

        em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями
        try {
            Transaction transaction = new Transaction(senderAccount.getClient(), senderAccount, beneficiaryAccount, sum); //пишем транзакцию
            em.persist(transaction); //сохраняем обьект

            if (!senderAccount.getCurrency().equals(beneficiaryAccount.getCurrency())) { //если валюты между счетами не совпадают, то
                String queryRateOfExchange = "SELECT r from RateOfExchange r WHERE currency = :currency"; //строка запроса в
                //таблицу курса валют - запрос на название валюты

                Query query = em.createQuery(queryRateOfExchange); //запрос
                query.setParameter("currency", senderAccount.getCurrency()); //для счета-отправителя
                RateOfExchange rateOfExchange = (RateOfExchange) query.getSingleResult(); //получаем одиночный результат
                double senderAccountRate = rateOfExchange.getRateToUAH(); //получаем курс счета-отправителя относительно к гривне

                query.setParameter("currency", beneficiaryAccount.getCurrency()); //для счета-получателя
                rateOfExchange = (RateOfExchange) query.getSingleResult(); //получаем одиночный результат
                double beneficiaryAccountRate = rateOfExchange.getRateToUAH(); //получаем курс счета-получателя относительно к гривне

                double thisBalance; //буфер для математических операций с средствами
                if (senderAccount.getCurrency().equals("UAH")) { //если валюта счета-отправителя является гривневой, то
                    thisBalance = sum / beneficiaryAccountRate; //инициализируем буфер = сумму перевода делим на курс счета-получателя
                    //относительно к гривне (т.е. конвертируем гривну к валюте, которая будет на счете-получателе)
                } else { //иначе
                    thisBalance = sum * senderAccountRate / beneficiaryAccountRate; //инициализируем буфер = сумму перевода
                    //умножаем на курс к гривне счета-отправителя и делим ее на курс к гривне счета-получателя (т.е. если мы
                    //отправлем 100 долларов на счет у которого валюта в евро - (100 * 36,56 / 39,83 = 91,79 евро) - получаем 91,79 евро)
                }
                senderAccount.withdrawFromBalance(sum); //со счета-отправителя снимаются отправленные средства(без конвертации)
                beneficiaryAccount.replenishBalance(thisBalance); //на счет-получатель добавляются средства из буфера(с конвертацией)
            } else { //иначе(т.е. если валюты совпадают между счетами)
                senderAccount.withdrawFromBalance(sum); //со счета-отправителя снимаются отправленные средства(без конвертации)
                beneficiaryAccount.replenishBalance(sum); //на счета-получатель добавляются отправленные средства(без конвертации)
            }
            em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями
            System.out.println("Ok!");
        } catch (Exception ex) {
            ex.printStackTrace();
            em.getTransaction().rollback(); //если возникла ошибка - отменяем все действия в изменениях сущностей
            return;
        }
    }

    private static void transferFundsWithConversionForSingleClient(Scanner sc) {
        System.out.print("Enter sender account id: "); //просим ввести id счета-отправителя
        String strSenderAccountId = sc.nextLine(); //считываем id счета-отправителя
        Long senderAccountId = Long.parseLong(strSenderAccountId); //парсим id счет-отправителя в число

        Account senderAccount = em.find(Account.class, senderAccountId); //находим счет-отправитель по его id
        if (senderAccount == null) { //если счет-отправитель отсутствует, то
            System.out.println("Account not found!"); //пишем - счет не найден
            return; //вернуться в меню
        }

        System.out.print("Enter beneficiary account id: "); //просим ввести id счета-получателя
        String strBeneficiaryAccountId = sc.nextLine(); //считываем id счета-получателя
        Long beneficiaryAccountId = Long.parseLong(strBeneficiaryAccountId); //парсим id счет-получателя в число

        Account beneficiaryAccount = em.find(Account.class, beneficiaryAccountId); //находим счет-получатель по его id
        if (beneficiaryAccount == null) { //если счет-получатель отсутствует, то
            System.out.println("Account not found!"); //пишем - счет не найден
            return; //вернуться в меню
        }

        if (senderAccount.getClient().equals(beneficiaryAccount.getClient())) { //если счета пренадлежат одному клиенту (находятся
            //в рамокач счетов одного клиента(пользователя))

            System.out.print("Enter sum for transfer: "); //просим ввести сумму для перевода между счетами
            String strSum = sc.nextLine(); //читаем ответ пользователя
            double sum = Double.parseDouble(strSum); //парсим переменную - сумма средств для перевода
            if (sum > senderAccount.getBalance()) { //если сумма перевода больше суммы на счете-отправителе, то
                System.out.println("Error! Insufficiently money!"); //Ошибка! Недостаточно средств!
                return; //возвращаемся в меню
            }

            em.getTransaction().begin(); //запускаем транзакцию для работы с сущностями
            try {
                Transaction transaction = new Transaction(senderAccount.getClient(), senderAccount, beneficiaryAccount, sum); //пишем транзакцию
                em.persist(transaction); //сохраняем обьект

                if (!senderAccount.getCurrency().equals(beneficiaryAccount.getCurrency())) { //если валюты между счетами не совпадают, то
                    String queryRateOfExchange = "SELECT r from RateOfExchange r WHERE currency = :currency"; //строка запроса в
                    //таблицу курса валют - запрос на название валюты

                    Query query = em.createQuery(queryRateOfExchange); //запрос
                    query.setParameter("currency", senderAccount.getCurrency()); //для счета-отправителя
                    RateOfExchange rateOfExchange = (RateOfExchange) query.getSingleResult(); //получаем одиночный результат
                    double senderAccountRate = rateOfExchange.getRateToUAH(); //получаем курс счета-отправителя относительно к гривне

                    query.setParameter("currency", beneficiaryAccount.getCurrency()); //для счета-получателя
                    rateOfExchange = (RateOfExchange) query.getSingleResult(); //получаем одиночный результат
                    double beneficiaryAccountRate = rateOfExchange.getRateToUAH(); //получаем курс счета-получателя относительно к гривне

                    double thisBalance; //буфер для математических операций с средствами
                    if (senderAccount.getCurrency().equals("UAH")) { //если валюта счета-отправителя является гривневой, то
                        thisBalance = sum / beneficiaryAccountRate; //инициализируем буфер = сумму перевода делим на курс счета-получателя
                        //относительно к гривне (т.е. конвертируем гривну к валюте, которая будет на счете-получателе)
                    } else { //иначе
                        thisBalance = sum * senderAccountRate / beneficiaryAccountRate; //инициализируем буфер = сумму перевода
                        //умножаем на курс к гривне счета-отправителя и делим ее на курс к гривне счета-получателя (т.е. если мы
                        //отправлем 100 долларов на счет у которого валюта в евро - (100 * 36,56 / 39,83 = 91,79 евро) - получаем 91,79 евро)
                    }
                    senderAccount.withdrawFromBalance(sum); //со счета-отправителя снимаются отправленные средства(без конвертации)
                    beneficiaryAccount.replenishBalance(thisBalance); //на счет-получатель добавляются средства из буфера(с конвертацией)
                } else { //иначе(т.е. если валюты совпадают между счетами)
                    senderAccount.withdrawFromBalance(sum); //со счета-отправителя снимаются отправленные средства(без конвертации)
                    beneficiaryAccount.replenishBalance(sum); //на счета-получатель добавляются отправленные средства(без конвертации)
                }
                em.getTransaction().commit(); //завершаем транзакцию для работы с сущностями
                System.out.println("Ok!");
            } catch (Exception ex) {
                ex.printStackTrace();
                em.getTransaction().rollback(); //если возникла ошибка - отменяем все действия в изменениях сущностей
                return;
            }
        } else { //иначе (т.е. у счетов разные клиенты)
            System.out.println("Error! Clients don't match!"); //печатаем - ошибка! клиенты не совпадают!
        }
    }

    private static void totalFundsSingleClientInUAH(Scanner sc) { //метод для отображения суммарных средств на счету одного
        // пользователя в UAH (расчет по курсу)
        System.out.print("Enter client name: "); //просим ввести имя клиента
        String strClientName = sc.nextLine(); //читаем имя введенное пользователем

        String queryAccounts = "SELECT a FROM Account a"; //строка-запрос к таблице счетов
        Query query = em.createQuery(queryAccounts, Account.class); //запрос
        List<Account> list = (List<Account>) query.getResultList(); //создаем список для результатов запроса

        double totalFunds = 0.0; //создаем переменную суммарноСредств

        for (Account account : list) { //для каждого счета из списка счетов(из таблицы)
            if (account.getClient().getName().equals(strClientName)) { //если имя существующего счета совпадает с именем
                //введенным пользователем, то
                account = em.getReference(Account.class, account.getId()); //выполняем поиск по id счета
                String queryRateOfExchange = "SELECT r from RateOfExchange r WHERE currency = :currency"; //строка запроса в
                //таблицу курса валют - запрос на название валюты
                query = em.createQuery(queryRateOfExchange); //запрос
                query.setParameter("currency", account.getCurrency()); //для счета
                RateOfExchange rateOfExchange = (RateOfExchange) query.getSingleResult(); //получаем одиночный результат
                double thisAccountRate = rateOfExchange.getRateToUAH(); //получаем курс счета относительно к гривне
                double balanceInUAH = thisAccountRate * account.getBalance(); //создаем переменную баланс в гривне = курс счета
                //к гривне умножаем на баланс счета
                totalFunds += balanceInUAH; //с каждым дополнительным счетом у одного клента - увеличиваем суммарные средства клиента
                System.out.println(account); //для сверки выводим счет
            } else { //иначе
                System.out.println("Account not found!"); //печатаем - счет не найден
                return; //возвращаемся в меню
            }
        }
        System.out.println("Total funds single client in UAH: " + String.format("%.2f", totalFunds)); //печатаем - сумма средств
        //одного клиента в гривне + результат, округленный до двух сотых (две цифры после запятой)
    }

    private static String selectCurrency() { //метод для выбора валюты (возвращает валюту в виде строки)
        String queryRate = "SELECT r FROM RateOfExchange r"; //строка-запрос для таблицы курсов валют
        Query query = em.createQuery(queryRate, RateOfExchange.class); //выполняем запрос
        List<RateOfExchange> list = (List<RateOfExchange>) query.getResultList(); //создаем список из курсов валют

        Scanner sc = new Scanner(System.in); //открываем поток для ввода данных с клавиатуры
        String strCurrencyNames = ""; //создаем пустую строку строкаНазванийВалют
        for (RateOfExchange rateOfExchange : list) { //проходимся с помощью for-each по каждому элементу из списка курса валют
            strCurrencyNames += rateOfExchange.getCurrency() + " "; //с каждым новым проходом конкатенируем строкуНазванийВалют
            //саму с собой через пробел
        }

        System.out.print("Select currency: " + strCurrencyNames); //предлогаем выбрать валюту из псевдо-списка валют(наша строкаНазванийВалют)
        String strCurrency = sc.nextLine(); //читаем ответ пользователя
        String currency = null; //создаем переменную валюта = null
        for (RateOfExchange rateOfExchange : list) { //проходимся с помощью for-each по каждому элементу из списка курса валют
            if (strCurrency.equals(rateOfExchange.getCurrency())) { //если введенная пользователем валюта совпадает с одной
                //валютой из списка валют, то
                currency = strCurrency; //присваиваем ее переменной валюта
            }
        }
        return currency; //возвращаем выбранную пользователем валюту
    }
}
//Создать базу данных «Банк» с таблицами «Пользователи», «Транзакции», «Счета» и «Курсы валют».
//Счет бывает 3-х видов: USD, EUR, UAH.
//Написать запросы:
// для пополнения счета в нужной валюте,
// перевода средств с одного счета на другой,
// конвертации валюты по курсу в рамках счетов одного пользователя.
//Написать запрос для получения суммарных средств на счету одного пользователя в UAH (расчет по курсу)

