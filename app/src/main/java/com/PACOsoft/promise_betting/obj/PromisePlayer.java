package com.PACOsoft.promise_betting.obj;

public class PromisePlayer {
    private String playerId;//방에 있는 사람 아이디
    private String nickName;//방에 있는 사람 닉네임
    private double x;//현재 이 사람이 위치한 x
    private double y;//현재 이 사람이 위치한 y
    private boolean arrival;//도착여부
    private int ranking; //이 사람의 랭킹
    public PromisePlayer(){
    }

    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getPlayerId() { return playerId; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getNickName() { return nickName; }
    public void setX(Double x) { this.x = x; }
    public Double getX() { return x; }
    public void setY(Double y) { this.y = y; }
    public Double getY() { return y; }
    public void setArrival(boolean arrival) { this.arrival = arrival; }
    public boolean getArrival() { return arrival; }
    public void setRanking(int ranking) { this.ranking = ranking; }
    public int getRanking() { return ranking; }
}
