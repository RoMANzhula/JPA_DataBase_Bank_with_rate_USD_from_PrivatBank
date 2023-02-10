package org.example;

import javax.persistence.*;

@Entity //аннотация, которая указывает Hibernate, что наш класс специальный и его обьекты нужно хранить в DataBase
@Table(name = "Rates_of_exchange") //устанавливаем название таблице
public class RateOfExchange {

    @Id //аннотация, с помощью которой задаем PrimaryKey
    @GeneratedValue //автогенерация номера ID
    @Column(name = "id_rate") //устанавливаем имя для колонки таблицы
    private Long id;

    @Column //колонка в таблице имеет название аналогично данному полю
    private String currency;

    @Column //колонка в таблице имеет название аналогично данному полю
    private Double rateToUAH;

    public RateOfExchange() {} //конструктор по-умолчанию

    public RateOfExchange(String currency, Double rateToUAH) { //конструктор данного класса с параметрами
        this.currency = currency;
        this.rateToUAH = rateToUAH;
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

    public Double getRateToUAH() {
        return rateToUAH;
    }

    public void setRateToUAH(Double rateToUAH) {
        this.rateToUAH = rateToUAH;
    }

    @Override //переопределяем метод
    public String toString() { //к строковому виду
        return "RateOfExchange{" +
                "id=" + id +
                ", currency='" + currency + '\'' +
                ", rateToUAH=" + rateToUAH +
                '}';
    }
}
