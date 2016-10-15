# INF321 - lab03VeV
Labórario 3 da extensão Eng Comp - INF 321

### Objetivo
Utilizar a ferramenta FindBugs para realizar a análise estática de código para detectar e remover
vulnerabilidades de segurança.

## Apresente uma solução para as vulnerabilidades de segurança detectadas no arquivo Database.java

Todas as configurações de banco devem ser transferidas para um arquivo de propriedades _(dbConnection.properties)_:

```
	driverName = oracle.jdbc.driver.OracleDriver
	url = jdbc:oracle:thin:@soa-sut-db.dei.uc.pt:1521:orcl
	userName = wsdbench
	passwd = Samsung
```

Esse arquivo deve ser restrito, e não deve estar junto com o repositório remoto, de modo que essas informações não sejam acessadas por qualquer um.
E assim, na classe *Database.java*, inserimos o seguinte trecho para resgatar essas propriedades e se conectar com o banco corretamente, como
segue a nova classe *DatabaseSecure.java*:

```Java
	FileInputStream in = new FileInputStream(System.getProperty("dbConnection.properties"));
	Properties prop = new Properties();
	prop.load(in);
	in.close();
	
	String driverName = prop.getProperty("driverName");
	String url = prop.getProperty("url");
	String userName = prop.getProperty("userName");
	String passwd = prop.getProperty("passwd");
```

## Selecione três vulnerabilidades do tipo SQL Injection (escolha situações variadas, e descreva sua estratégia de seleção).
	Pending....

## Para cada vulnerabilidade selecionada, (1) defina dois casos de teste que explorem a vulnerabilidade e (2) apresente uma versão do código que elimine o bug.
	Pending....
