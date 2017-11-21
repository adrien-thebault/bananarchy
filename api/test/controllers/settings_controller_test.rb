require 'test_helper'

class SettingsControllerTest < ActionDispatch::IntegrationTest
  setup do
    @setting = settings(:one)
  end

  test "should get index" do
    get settings_url, as: :json
    assert_response :success
  end

  test "should create setting" do
    assert_difference('Setting.count') do
      post settings_url, params: { setting: { name: @setting.name, value: @setting.value } }, as: :json
    end

    assert_response 201
  end

  test "should show setting" do
    get setting_url(@setting), as: :json
    assert_response :success
  end

  test "should update setting" do
    patch setting_url(@setting), params: { setting: { name: @setting.name, value: @setting.value } }, as: :json
    assert_response 200
  end

  test "should destroy setting" do
    assert_difference('Setting.count', -1) do
      delete setting_url(@setting), as: :json
    end

    assert_response 204
  end
end
