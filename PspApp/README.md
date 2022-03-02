# PspApp
PspApp (The RTP app maintains a three layered structure), which is described below:
  - SPL (spl): This is the innermost layer and is the most important part of the RTP framework. This layer is responsible for maintaining the security and encryption functionalities. Any one wants to integrate the issuer app has to take at least this layer. This layer communicates directly with the core and also authenticate the user.
  
  - SDK (pspsdk): This is the middle layer which communicates with the PSP server and the SPL. SDK initiates the transaction request by calling the appropriate PSP api and also calls the SPL for authentication. After getting back the result from the SPL, this layer further calls the appropriate PSP api for the final status of the transaction and finally shows it to the user.
  
  - Base App (app): This is the outermost layer. When the user tries to access any RTP feature like PAY, COLLECT or BALANCE ENQUIRY the request generates from this layer and propagates to the SDK layer. After each transaction user will come back to this layer.
  
