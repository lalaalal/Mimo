package com.lalaalal.mimo.console.option;

public interface Option {
    void handle(OptionConsumer optionConsumer);

    String[] help();
}
