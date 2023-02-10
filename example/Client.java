package org.example;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity //аннотация, которая указывает Hibernate, что наш класс специальный и его обьекты нужно хранить в DataBase
@Table(name = "Clients") //устанавливаем название таблице
public class Client {
    @Id //аннотация, с помощью которой задаем PrimaryKey
    @GeneratedValue //автогенерация номера ID
    @Column(name = "id_Client") //устанавливаем имя для колонки таблицы
    private Long id;

    @Column(name = "name_Client", nullable = false) //устанавливаем имя для колонки таблицы, которо не межет быть пустым
    private String name;

    @Column(name = "phone_Client", unique = true) //устанавливаем имя для колонки таблицы, поле должно быть уникальным
    private Integer phone;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL) //CascadeType.ALL означает, что все действия, которые мы
    // выполняем с родительским объектом, нужно повторить и для его зависимых объектов.
    private List<Account> accounts = new ArrayList<>();

    public Client() {} //конструктор по-умолчанию

    public Client(String name, Integer phone) { //конструктор данного класса с параметрами для будущих обьектов
        this.name = name;
        this.phone = phone;
    }

    //Геттеры и Сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void addAccount(Account account) { //метод для добавления счета в список счетов
        if(!accounts.contains(account)) {
            accounts.add(account);
            account.setClient(this);
        }
    }

    @Override //переопределяем метод
    public String toString() { //к строковому виду
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone=" + phone +
                '}';
    }
}
