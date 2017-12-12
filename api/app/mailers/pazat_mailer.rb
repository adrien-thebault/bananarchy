class PazatMailer < ApplicationMailer

  def oops

    mail(to: "adrien.thebault@insa-rennes.fr",
         body: "Bonjoure,<br />

         Je ne serait pas présant se matein en court.<br /><br />

         Caurdialemment,<br />
         Alexis Busseneau.<br />",
        content_type: "text/html",
        subject: "[bananarchy] J'ai la flemme")

  end

end
