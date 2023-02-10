package org.example;

import javax.persistence.*;

@Entity //аннотация, которая указывает Hibernate, что наш класс специальный и его обьекты нужно хранить в DataBase
@Table(name = "Accounts") //устанавливаем название таблице
public class Account {
    @Id //аннотация, с помощью которой задаем PrimaryKey
    @GeneratedValue //автогенерация номера ID
    @Column(name = "id_Account") //устанавливаем имя для колонки таблицы
    private Long id;

    @Column(name = "currency_account") //устанавливаем имя для колонки таблицы
    private String currency;

    @Column(name = "balance_account") //устанавливаем имя для колонки таблицы
    private Double balance;

    @ManyToOne //аннотация @ManyToOne говорит Hibernate, что много сущностей из других таблиц могут ссылаться на одну сущность Client
    @JoinColumn(name = "id_Client", nullable = false) //аннотация @JoinColumn указывает имя колонки, из которой будет браться id,
    //не может быть null
    private Client client;

    public Account() {} //конструктор по-умолчанию

    public Account(String currency, Double balance, Client client) { //конструктор данного класса с параметрами для будущих обьектов
        this.currency = currency;
        this.balance = balance;
        this.client = client;
    }

    //Геттеры и Сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double total) {
        this.balance = total;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void replenishBalance(Double balance) { // метод для пополнения баланса
        this.balance += balance;
    }

    public void withdrawFromBalance(Double balance) { //метод для снятия с баланса
        this.balance -= balance;
    }

    @Override //переопределяем метод
    public String toString() { //к строковому виду
        return "Account{" +
                "id=" + id +
                ", currency='" + currency + '\'' +
                ", balance=" + balance +
                ", client=" + client +
                '}';
    }
}
