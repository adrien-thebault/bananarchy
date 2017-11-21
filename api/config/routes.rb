Rails.application.routes.draw do

  resources :settings, only: [:index, :show, :update]

  get 'data/all', to:'data#all'
  get 'data/weather', to:'data#weather'
  get 'data/agenda', to:'data#agenda'
  get 'data/travel_time', to:'data#travel_time'
  get 'data/timestamp', to:'data#timestamp'
  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
end
