class DataController < ApplicationController

  API_KEY = "AIzaSyA6MgLEvkwT3YZBvoS-p_-wMVqAgFPQf9s"

  before_action :get_settings

  # GET /data/all
  def all

    render :json => {
      :agenda => get_agenda,
      :weather => get_weather,
      :travel_time => (@settings[:transportation] == 'bus') ? get_travel_time_bus : get_travel_time_car,
      :timestamp => Time.now.getutc.to_i
    }

  end

  # GET /data/weather
  def weather
    render :json => get_weather
  end

  # GET /data/agenda
  def agenda
    render :json => get_agenda
  end

  # GET /data/travel_time
  def travel_time
    render :json => (@settings[:transportation] == 'bus') ? get_travel_time_bus : get_travel_time_car
  end

  # GET /data/timestamp
  def timestamp
    render :json => Time.now.getutc.to_i
  end

  private

    def get_settings

      @settings = {}
      Setting.all.each do |s|
        @settings[s.name.to_sym] = s.value
      end

      @settings

    end

    # Agenda
    def get_agenda
      JSON.parse(RestClient.get("https://edt.adrien-thebault.fr/api/calendar.json?begin=#{Time.now.getutc.to_i.to_s}&end=#{(Time.now.getutc.to_i + 86400).to_s}&resources=#{@settings[:agenda]}"), :symbolize_names => true)[:calendar].first do |x|
        !x.ended
      end
    end

    # Weather
    def get_weather

      lat, lng = @settings[:location].split(',');
      res = JSON.parse(RestClient.get("api.openweathermap.org/data/2.5/weather?APPID=e37530147ce948846caa578db826f263&lat=#{lat}&lon=#{lng}"), :symbolize_names => true)

      {
        :weather => res[:weather][0][:main],
        :temp => (res[:main][:temp].to_f - 273.15).round
      }

    end

    # Time w/ bus
    def get_travel_time_bus
      ((JSON.parse(RestClient.get("https://maps.googleapis.com/maps/api/directions/json?origin=#{@settings[:location]}&destination=#{@settings[:workplace]}&mode=transit&transit_mode=bus&key=#{API_KEY}"), :symbolize_names => true)[:routes][0][:legs][0][:duration][:value].to_i)/60).to_i
    end

    # Time w/ car
    def get_travel_time_car
      ((JSON.parse(RestClient.get("https://maps.googleapis.com/maps/api/directions/json?origin=#{@settings[:location]}&destination=#{@settings[:workplace]}&key=#{API_KEY}"), :symbolize_names => true)[:routes][0][:legs][0][:duration][:value].to_i)/60).to_i
    end

end
