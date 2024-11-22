package utils

import models.{Email, Guest, GuestInfo}

import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Authenticator, Message, MessagingException, PasswordAuthentication, Session, Transport}

object MailUtil {
  val properties: Properties = new Properties()
  properties.put("mail.smtp.host", "smtp.gmail.com") // Replace with your SMTP server
  properties.put("mail.smtp.port", "587")
  properties.put("mail.smtp.auth", "true")
  properties.put("mail.smtp.starttls.enable", "true")
  val session = Session.getInstance(properties, new Authenticator() {
    override protected def getPasswordAuthentication =
      new PasswordAuthentication("rupal.gupta240897@gmail.com", "***********")
  })

  def composeMail(guest: GuestInfo): Email = {

    val content: String = s"""
                             |<html>
                             |<head>
                             |  <title>Restaurant Menu</title>
                             |  <style>
                             |    body {
                             |      font-family: 'Arial', sans-serif;
                             |      background-color: #f9f9f9;
                             |      color: #333;
                             |      margin: 0;
                             |      padding: 0;
                             |    }
                             |
                             |    h2 {
                             |      color: #2c3e50;
                             |      text-align: center;
                             |      font-size: 24px;
                             |      margin-top: 20px;
                             |    }
                             |
                             |    p {
                             |      text-align: center;
                             |      font-size: 18px;
                             |      color: #7f8c8d;
                             |    }
                             |
                             |    ul {
                             |      list-style-type: none;
                             |      padding: 0;
                             |      max-width: 600px;
                             |      margin: 20px auto;
                             |    }
                             |
                             |    li {
                             |      background-color: #ffffff;
                             |      margin: 10px 0;
                             |      padding: 15px;
                             |      border-radius: 8px;
                             |      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                             |      transition: background-color 0.3s, transform 0.2s;
                             |    }
                             |
                             |    li:hover {
                             |      background-color: #ecf0f1;
                             |      transform: scale(1.02);
                             |    }
                             |
                             |    li span {
                             |      font-weight: bold;
                             |      color: #3498db;
                             |    }
                             |  </style>
                             |</head>
                             |<body>
                             |  <h2>Hello ${guest.name}</h2>
                             |
                             |  <h2>Please check out our restaurant's menu for today:</h2>
                             |  <ul>
                             |    <li>1. Food item - Pizza, Price - 250</li>
                             |    <li>2. Food item - Pasta, Price - 300</li>
                             |    <li>3. Food item - Sandwich, Price - 350</li>
                             |    <li>4. Food item - Tomato Soup, Price - 200</li>
                             |    <li>5. Food item - Mushroom Soup, Price - 400</li>
                             |  </ul>
                             |
                             |  <p>Best Regards,</p>
                             |  <p>Hotel Grand</p>
                             |</body>
                             |</html>
                             |
    """.stripMargin
    Email(guest.email, "Restaurant Menu", content)
  }

  def composeAndSendEmail(guestInfo :GuestInfo): Unit = {
    val mailContent = composeMail(guestInfo)
    sendEmail(mailContent)
  }

  def composeAndSendEmailAllGuests(guestList: Seq[Guest]): Unit = {
    guestList.foreach(guest => (GuestInfo(guest.name, guest.email)))
  }

  private def sendEmail(email: Email): Unit = {

    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress("rupal.gupta240897@gmail.com"))
      message.setRecipients(Message.RecipientType.TO, email.receiverId)
      message.setSubject(email.subject)
      message.setContent(email.body, "text/html; charset=utf-8")
      Transport.send(message)
      println(s"Email sent to ${email.receiverId}")
    } catch {
      case e: MessagingException =>
        e.printStackTrace()
    }
  }

}
