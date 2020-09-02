# -*- coding: utf-8 -*-

import pandas as pd
import torch
import torch.nn as nn
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import torch.nn.functional as F
from torch import optim



xHarData = pd.read_csv('UCI HAR Dataset/UCI HAR Dataset/train/X_train.txt', header=None)
yHarData = pd.read_csv('UCI HAR Dataset/UCI HAR Dataset/train/y_train.txt', header=None)

harData = xHarData
harData['Activity'] = yHarData[0]

print(harData)

harData.Activity.value_counts().plot(kind='pie', autopct='%1.0f%%', colors=['skyblue', 'orange', 'red', 'green', 'yellow', 'maroon'], explode=(0.05, 0.05, 0.05, 0.05, 0.05, 0.05))

def transform(x):
 
    x = (x.split(' '))
    ans = [] 
    
    for i, data in enumerate(x):
        if not data == '':
            ans.append(float(data))
       
    return ans


newData =[]

for i in range(len(harData[0])):
    newData.append((transform(harData[0][i]))[:300])


inputs = torch.tensor(newData[:6700])
#print(inputs)

outputs = torch.tensor(harData['Activity'][:6700].values).flatten()
#print(outputs)

#print(inputs.shape)
#print(outputs.shape)

x_test = torch.tensor(newData[6701:])

y_test = torch.tensor(harData['Activity'][6701:].values).flatten()

#print(x_test.shape)
#print(y_test.shape)


class Network(nn.Module):
    def __init__(self):
        super().__init__()
        # Inputs to hidden layer linear transformation
        self.h1 = nn.Linear(300, 512)
              
        # Output layer, 10 units - one for each digit
        self.output = nn.Linear(512, 7)
        
    def forward(self, x):
               
        # Hidden layer with sigmoid activation
        x = F.dropout(F.relu(self.h1(x)))
        
        # Output layer with softmax activation
        x = F.softmax(self.output(x), dim=1)
        
        return x


model = Network()


criterion = nn.CrossEntropyLoss()
optimizer = optim.Adam(model.parameters(), lr=0.003)

epochs = 100
train_losses, test_losses = [], []
batch_size = 16

for e in range(epochs):
    
    running_loss = 0 
    
    permutation = torch.randperm(inputs.size()[0])
    
    for i in range(0, inputs.size()[0], batch_size):
        
        optimizer.zero_grad()
        
        indices = permutation[i:i+batch_size]
        batch_x, batch_y = inputs[indices], outputs[indices]
        
        log_ps = model(batch_x)
        loss = criterion(log_ps, batch_y)
        loss.backward()
        optimizer.step()
    
        running_loss += loss.item()
        
    else:
        
        test_loss = 0
        accuracy = 0
        
        permutation = torch.randperm(x_test.size()[0])
    
        for i in range(0, x_test.size()[0], batch_size):
        
            # Turn off gradients for validation, saves memory and computations
            with torch.no_grad():
                
                model.eval()
                indices = permutation[i:i+batch_size]
                batch_x, batch_y = x_test[indices], y_test[indices]
                log_ps = model(batch_x)
                test_loss += criterion(log_ps, batch_y)
                
                ps = torch.exp(log_ps)
                top_p, top_class = ps.topk(1, dim=1)
                equals = top_class == batch_y.view(*top_class.shape)
                accuracy += torch.mean(equals.type(torch.FloatTensor))
        
        model.train()
        
        train_losses.append(running_loss/batch_size)
        test_losses.append(test_loss/batch_size)

        print("Epoch: {}/{}.. ".format(e+1, epochs),
              "Training Loss: {:.3f}.. ".format(running_loss/batch_size),
              "Test Loss: {:.3f}.. ".format(test_loss/batch_size),
              "Test Accuracy: {:.3f}".format(accuracy/batch_size))
