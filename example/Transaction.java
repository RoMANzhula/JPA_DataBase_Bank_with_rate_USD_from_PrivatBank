package org.example;

import javax.persistence.*;

@Entity //аннотация, которая указывает Hibernate, что наш класс специальный и его обьекты нужно хранить в DataBase
@Table(name = "Transactions") //устанавливаем название таблице
public class Transaction {
    @Id //аннотация, с помощью которой задаем PrimaryKey
    @GeneratedValue //автогенерация номера ID
    @Column(name = "id_Transaction") //устанавливаем имя для колонки таблицы
    private Long id;

    @ManyToOne() //аннотация @ManyToOne говорит Hibernate, что много сущностей из других таблиц могут ссылаться на одну сущность Client
    @JoinColumn(name = "client_Id") //аннотация @JoinColumn указывает имя колонки, из которой будет браться id
    private Client client;

    @ManyToOne //аннотация @ManyToOne говорит Hibernate, что много сущностей из других таблиц могут ссылаться на одну сущность Account
    @JoinColumn(name = "beneficiary_Id_Account") //аннотация @JoinColumn указывает имя колонки, из которой будет браться id
    private Account beneficiaryIdAccount;

    @ManyToOne //аннотация @ManyToOne говорит Hibernate, что много сущностей из других таблиц могут ссылаться на одну сущность Account
    @JoinColumn(name = "sender_Id_Account") //аннотация @JoinColumn указывает имя колонки, из которой будет браться id
    private Account senderIdAccount;

    @Column(name = "balancePlus_Transaction") //устанавливаем имя для колонки таблицы
    private Double balancePlus;

    public Transaction() {} //конструктор по-умолчанию

    //конструктор данного класса с параметрами для будущих обьектов
    public Transaction(Client client, Account beneficiaryIdAccount, Account senderIdAccount, Double balancePlus) {
        this.client = client;
        this.beneficiaryIdAccount = beneficiaryIdAccount;
        this.senderIdAccount = senderIdAccount;
        this.balancePlus = balancePlus;
    }

    //Геттеры и Сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Account getBeneficiaryIdAccount() {
        return beneficiaryIdAccount;
    }

    public void setBeneficiaryIdAccount(Account beneficiaryIdAccount) {
        this.beneficiaryIdAccount = beneficiaryIdAccount;
    }

    public Account getSenderIdAccount() {
        return senderIdAccount;
    }

    public void setSenderIdAccount(Account senderIdAccount) {
        this.senderIdAccount = senderIdAccount;
    }

    public Double getBalancePlus() {
        return balancePlus;
    }

    public void setBalancePlus(Double balancePlus) {
        this.balancePlus = balancePlus;
    }

    @Override //переопределяем метод
    public String toString() { //к строковому виду
        return "Transaction{" +
                "id=" + id +
                ", client=" + client +
                ", beneficiaryIdAccount=" + beneficiaryIdAccount +
                ", senderIdAccount=" + senderIdAccount +
                ", balancePlus=" + balancePlus +
                '}';
    }
}
