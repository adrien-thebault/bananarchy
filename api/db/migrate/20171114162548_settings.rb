class Settings < ActiveRecord::Migration[5.1]
  def change

    Setting.new(name: 'transportation', value: '').save!
    Setting.new(name: 'agenda', value: '').save!
    Setting.new(name: 'location', value: '').save!

  end
end
