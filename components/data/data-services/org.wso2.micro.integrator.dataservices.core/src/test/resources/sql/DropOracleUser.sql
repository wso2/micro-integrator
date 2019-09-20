DECLARE
	u_count number;
	user_name VARCHAR2 (50);
 
	BEGIN

		u_count :=0;
		user_name :='datauser';
        	SELECT COUNT (1) INTO u_count FROM dba_users WHERE username = UPPER (user_name);
 
     		IF u_count != 0
     		THEN
         		EXECUTE IMMEDIATE ('DROP USER '||user_name||' CASCADE');
      		END IF;
 
      		u_count := 0;
		
		EXCEPTION
   		WHEN OTHERS
      		THEN
         			DBMS_OUTPUT.put_line (SQLERRM);
         			DBMS_OUTPUT.put_line ('   ');
 
	END;

/