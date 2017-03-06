package com.company;


class DFM extends FileType{

    Integer[] data;
    Integer id;

    DFM(String[] data)
    {
        this.data = new Integer[data.length - 1];
        for(int i = 1; i < data.length; i ++)
        {
            this.data[i - 1] = Integer.parseInt(data[i]);
        }
        this.id = Integer.parseInt(data[0]);
    }
}
