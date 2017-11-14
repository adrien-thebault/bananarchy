class SettingsController < ApplicationController
  before_action :set_setting, only: [:show, :update]

  # GET /settings
  def index
    @settings = Setting.all
    render json: @settings
  end

  # GET /settings/{name}
  def show
    render json: @setting
  end

  # PATCH/PUT /settings/{name}
  def update
    if @setting.update(setting_params)
      render json: @setting
    else
      render json: @setting.errors, status: :unprocessable_entity
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_setting
      @setting = Setting.where(name: params[:id]).first
    end

    # Only allow a trusted parameter "white list" through.
    def setting_params
      params.require(:setting).permit(:name, :value)
    end
end
