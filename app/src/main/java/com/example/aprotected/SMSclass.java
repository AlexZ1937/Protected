package com.example.aprotected;

public class SMSclass
{
    String sender;
    String text;
    public SMSclass(String sender,String text)
    {
        this.sender=sender;
        this.text=text;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }
}
