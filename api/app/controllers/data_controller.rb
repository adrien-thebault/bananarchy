class DataController < ApplicationController

  API_KEY = "AIzaSyA6MgLEvkwT3YZBvoS-p_-wMVqAgFPQf9s"

  before_action :get_settings

  # GET /data/all
  def all

    render :json => {
      :agenda => get_agenda,
      :weather => get_weather,
      :travel_time => get_travel_time,
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
    render :json => get_travel_time
  end

  # GET /data/timestamp
  def timestamp
    render :json => Time.now.getutc.to_i
  end

  # POST /data/send_mail
  def send_mail
    PazatMailer.oops.deliver_now
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

      # If true, the API will generate a fake agenda entry
      demo = true

      if demo

        travel_time = get_travel_time.to_i*60

        {               # current_date + margin + 2 min for the demo + preparation_time + travel_time
          :beginning => Time.now.getutc.to_i + 300 + 120 + @settings[:preparation_time].to_i*60 + travel_time,
          :end => Time.now.getutc.to_i + 300 + 120 + @settings[:preparation_time].to_i*60 + travel_time + 3600,
          :duration => 3600,
          :summary => "Démo IOT",
          :location => "ICI",
          :groups => ["S9-INFO"],
          :details => "Démonstration IOT",
          :teacher => "Moi",
          :current => false,
          :ended => false
        }

      else

        res = JSON.parse(RestClient.get("https://edt.adrien-thebault.fr/api/calendar.json?begin=#{Time.now.getutc.to_i.to_s}&end=#{(Time.now.getutc.to_i + 7*86400).to_s}&resources=#{@settings[:agenda]}"), :symbolize_names => true)[:calendar]
        is_first_of_today_ended = false
        today = Time.now.beginning_of_day.getutc.to_i
        tomorrow = today + 86400

        res.each do |x|
            if x[:beginning] >= today && x[:beginning] < tomorrow && x[:ended]
              is_first_of_today_ended = true
              break
            end
        end

        (res.select do |x|
          puts x[:ended]
          !x[:ended] && ((is_first_of_today_ended && x[:beginning] >= tomorrow) || !is_first_of_today_ended)
        end).first

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

    # Time
    def get_travel_time
      if(@settings[:transportation] == 'bus')
        ((JSON.parse(RestClient.get("https://maps.googleapis.com/maps/api/directions/json?origin=#{@settings[:location]}&destination=#{@settings[:workplace]}&mode=transit&transit_mode=bus&key=#{API_KEY}"), :symbolize_names => true)[:routes][0][:legs][0][:duration][:value].to_i)/60).to_i
      elsif(@settings[:transportation] == 'car')
        ((JSON.parse(RestClient.get("https://maps.googleapis.com/maps/api/directions/json?origin=#{@settings[:location]}&destination=#{@settings[:workplace]}&key=#{API_KEY}"), :symbolize_names => true)[:routes][0][:legs][0][:duration][:value].to_i)/60).to_i
      else
        ((JSON.parse(RestClient.get("https://maps.googleapis.com/maps/api/directions/json?origin=#{@settings[:location]}&destination=#{@settings[:workplace]}&mode=walking&key=#{API_KEY}"), :symbolize_names => true)[:routes][0][:legs][0][:duration][:value].to_i)/60).to_i
      end
    end

end
